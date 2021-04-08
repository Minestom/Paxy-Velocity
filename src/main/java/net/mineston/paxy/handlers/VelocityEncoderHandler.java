package net.mineston.paxy.handlers;

import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.handshake.HandshakeIntent;
import com.github.steveice10.mc.protocol.packet.handshake.client.HandshakePacket;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.packetlib.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.mineston.paxy.utils.ProtocolUtils;

import java.util.List;

public class VelocityEncoderHandler extends MessageToMessageEncoder<ByteBuf> {

    private final PaxyProtocol protocol;

    protected VelocityEncoderHandler(PaxyProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        msg.retain();
        {
            ByteBuf packetBuf = msg.retainedSlice();
            Packet packet = ProtocolUtils.readPacket(protocol, packetBuf);

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
        }

        out.add(msg);
    }
}
