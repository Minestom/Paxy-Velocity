package net.mineston.paxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.logging.Logger;

@Plugin(id = "paxy", name = "Paxy", version = "0.1.0-SNAPSHOT",
        description = "I did it!", authors = {"Me"})
public class PaxyMain {

    public static ProxyServer PROXY;

    private ProxyServer server;

    @Inject
    public PaxyMain(ProxyServer server, Logger logger) {
        this.server = server;
        logger.info("Hello there! I made my first plugin with Velocity.");
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        PROXY = server;
        System.out.println("ADD LISTENER");
        var injector = new VelocityInjector();

        try {
            injector.inject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
