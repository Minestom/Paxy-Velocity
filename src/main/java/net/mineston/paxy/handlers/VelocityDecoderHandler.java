package net.mineston.paxy.handlers;

import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.mineston.paxy.utils.ProtocolUtils;

import java.util.List;

public class VelocityDecoderHandler extends MessageToMessageDecoder<ByteBuf> {

    private final PaxyProtocol protocol;

    protected VelocityDecoderHandler(PaxyProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        msg.retain();
        {
            ByteBuf packetBuf = msg.retainedSlice();
            Packet packet = ProtocolUtils.readPacket(protocol, packetBuf);

            System.out.println("server packet: " + packet.getClass().getSimpleName());

            if (packet instanceof LoginSuccessPacket) {
                protocol.setSubProtocol(SubProtocol.GAME);
            }
        }

        out.add(msg);
    }
}
