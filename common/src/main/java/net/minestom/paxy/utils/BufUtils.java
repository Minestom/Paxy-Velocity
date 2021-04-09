package net.minestom.paxy.utils;

import io.netty.buffer.ByteBuf;

public class BufUtils {

    public static void writeVarInt(ByteBuf buffer, int value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buffer.writeByte(temp);
        } while (value != 0);
    }

    public static int readVarInt(ByteBuf buffer) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buffer.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

}
