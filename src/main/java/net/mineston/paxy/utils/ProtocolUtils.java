package net.mineston.paxy.utils;

import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetInput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import io.netty.buffer.ByteBuf;
import net.mineston.paxy.handlers.PaxyProtocol;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ProtocolUtils {

    public static @NotNull Packet readPacket(@NotNull PaxyProtocol protocol, int packetId, @NotNull ByteBuf packetBuf) {
        Packet packet = protocol.createIncomingPacket(packetId);
        try {
            packet.read(new ByteBufNetInput(packetBuf));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packet;
    }

    public static void writePacket(@NotNull Packet packet, @NotNull ByteBuf packetBuf) {
        try {
            packet.write(new ByteBufNetOutput(packetBuf));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
