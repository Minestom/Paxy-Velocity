package net.mineston.paxy.handlers;

import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.handshake.HandshakeIntent;
import com.github.steveice10.mc.protocol.packet.handshake.client.HandshakePacket;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.mineston.paxy.utils.BufUtils;

import java.io.IOException;
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
            final int packetId = BufUtils.readVarInt(packetBuf);
            Packet packet = protocol.createIncomingPacket(packetId);

            try {
                packet.read(new ByteBufNetInput(packetBuf));
            } catch (IOException e) {
                e.printStackTrace();
            }

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
