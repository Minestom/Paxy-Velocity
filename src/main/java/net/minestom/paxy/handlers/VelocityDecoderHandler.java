package net.minestom.paxy.handlers;

import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minestom.paxy.utils.BufUtils;
import net.minestom.paxy.utils.PipelineUtil;
import net.minestom.paxy.utils.ProtocolUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@ChannelHandler.Sharable
public class VelocityDecoderHandler extends MessageToMessageDecoder<ByteBuf> {

    private final PaxyProtocol protocol;
    private boolean handledCompression;
    private boolean skipDoubleTransform;

    protected VelocityDecoderHandler(PaxyProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        if (skipDoubleTransform) {
            skipDoubleTransform = false;
            out.add(msg.retain());
            return;
        }

        {
            ByteBuf transformedBuf = msg.retain();

            boolean needsCompress = handleCompressionOrder(ctx, transformedBuf);
            final int packetId = BufUtils.readVarInt(transformedBuf);
            Packet packet = ProtocolUtils.readPacket(protocol, packetId, transformedBuf);

            System.out.println("server packet: " + packet.getClass().getSimpleName());

            if (packet instanceof LoginSuccessPacket) {
                protocol.setSubProtocol(SubProtocol.GAME);
            }

            // TODO script transform

            // TODO check if packet changed
            ByteBuf buffer = ctx.alloc().buffer();
            BufUtils.writeVarInt(buffer, packetId);
            ProtocolUtils.writePacket(packet, buffer);
            transformedBuf = buffer;

            if (needsCompress) {
                recompress(ctx, transformedBuf);
                skipDoubleTransform = true;
            }

            out.add(transformedBuf);
        }
    }

    private boolean handleCompressionOrder(ChannelHandlerContext ctx, ByteBuf buf) throws InvocationTargetException {
        if (handledCompression) return false;

        int decoderIndex = ctx.pipeline().names().indexOf("compression-decoder");
        if (decoderIndex == -1) return false;
        handledCompression = true;
        if (decoderIndex > ctx.pipeline().names().indexOf(Handlers.DECODER)) {
            // Need to decompress this packet due to bad order
            ByteBuf decompressed = (ByteBuf) PipelineUtil.callDecode((MessageToMessageDecoder<?>) ctx.pipeline().get("compression-decoder"), ctx, buf).get(0);
            try {
                buf.clear().writeBytes(decompressed);
            } finally {
                decompressed.release();
            }
            ChannelHandler encoder = ctx.pipeline().get(Handlers.ENCODER);
            ChannelHandler decoder = ctx.pipeline().get(Handlers.DECODER);
            ctx.pipeline().remove(encoder);
            ctx.pipeline().remove(decoder);
            ctx.pipeline().addAfter("compression-encoder", Handlers.ENCODER, encoder);
            ctx.pipeline().addAfter("compression-decoder", Handlers.DECODER, decoder);
            return true;
        }
        return false;
    }

    private void recompress(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        ByteBuf compressed = ctx.alloc().buffer();
        try {
            PipelineUtil.callEncode((MessageToByteEncoder<?>) ctx.pipeline().get("compression-encoder"), ctx, buf, compressed);
            buf.clear().writeBytes(compressed);
        } finally {
            compressed.release();
        }
    }

}
