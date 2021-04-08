package net.mineston.paxy.utils;

import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetInput;
import io.netty.buffer.ByteBuf;
import net.mineston.paxy.handlers.PaxyProtocol;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ProtocolUtils {

    public static @NotNull Packet readPacket(@NotNull PaxyProtocol protocol, @NotNull ByteBuf packetBuf) {
        final int packetId = BufUtils.readVarInt(packetBuf);
        Packet packet = protocol.createIncomingPacket(packetId);
        try {
            packet.read(new ByteBufNetInput(packetBuf));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packet;
    }

}
