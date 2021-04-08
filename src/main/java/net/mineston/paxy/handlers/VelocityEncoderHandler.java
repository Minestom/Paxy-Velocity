package net.mineston.paxy.handlers;

import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.handshake.HandshakeIntent;
import com.github.steveice10.mc.protocol.packet.handshake.client.HandshakePacket;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.packetlib.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.mineston.paxy.utils.PipelineUtil;
import net.mineston.paxy.utils.ProtocolUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@ChannelHandler.Sharable
public class VelocityEncoderHandler extends MessageToMessageEncoder<ByteBuf> {

    private final PaxyProtocol protocol;
    private boolean handledCompression;

    protected VelocityEncoderHandler(PaxyProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws InvocationTargetException {
        msg.retain();
        {
            ByteBuf transformedBuf = msg.copy();

            boolean needsCompress = handleCompressionOrder(ctx, transformedBuf);

            Packet packet = ProtocolUtils.readPacket(protocol, transformedBuf);

            System.out.println("client packet: " + packet.getClass().getSimpleName());

            if (packet instanceof HandshakePacket) {
                HandshakePacket handshakePacket = (HandshakePacket) packet;
                HandshakeIntent intent = handshakePacket.getIntent();
                if (intent == HandshakeIntent.LOGIN) {
                    protocol.setSubProtocol(SubProtocol.LOGIN);
                } else if (intent == HandshakeIntent.STATUS) {
                    protocol.setSubProtocol(SubProtocol.STATUS);
                }
            } else if (packet instanceof LoginStartPacket) {
                protocol.setSubProtocol(SubProtocol.GAME);
            }

            if (needsCompress) {
                recompress(ctx, transformedBuf);
            }
        }

        out.add(msg);
    }

    private boolean handleCompressionOrder(ChannelHandlerContext ctx, ByteBuf buf) throws InvocationTargetException {
        if (handledCompression) return false;

        int encoderIndex = ctx.pipeline().names().indexOf("compression-encoder");
        if (encoderIndex == -1) return false;
        handledCompression = true;
        if (encoderIndex > ctx.pipeline().names().indexOf(Handlers.ENCODER)) {
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

    private void recompress(ChannelHandlerContext ctx, ByteBuf buf) throws InvocationTargetException {
        ByteBuf compressed = ctx.alloc().buffer();
        try {
            PipelineUtil.callEncode((MessageToByteEncoder<?>) ctx.pipeline().get("compression-encoder"), ctx, buf, compressed);
            buf.clear().writeBytes(compressed);
        } finally {
            compressed.release();
        }
    }

}
