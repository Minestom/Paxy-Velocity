package net.mineston.paxy.handlers;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;

public class PaxyProtocol extends MinecraftProtocol {

    private final boolean client;

    public PaxyProtocol(SubProtocol subProtocol, boolean client) {
        this.client = client;
        setSubProtocol(subProtocol, client, null);
    }

    public void setSubProtocol(SubProtocol subProtocol) {
        setSubProtocol(subProtocol, client, null);
    }
}
