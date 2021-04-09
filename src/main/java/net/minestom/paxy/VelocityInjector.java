package net.minestom.paxy;

import io.netty.channel.ChannelInitializer;
import net.minestom.paxy.handlers.VelocityChannelInitializer;
import net.minestom.paxy.utils.ReflectionUtil;

import java.lang.reflect.Method;

public class VelocityInjector {

    public static Method getPlayerInfoForwardingMode;

    static {
        try {
            getPlayerInfoForwardingMode = Class.forName("com.velocitypowered.proxy.config.VelocityConfiguration")
                    .getMethod("getPlayerInfoForwardingMode");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ChannelInitializer<?> getBackendInitializer() throws Exception {
        Object connectionManager = ReflectionUtil.get(PaxyMain.PROXY, "cm", Object.class);
        Object channelInitializerHolder = ReflectionUtil.invoke(connectionManager, "getBackendChannelInitializer");
        return (ChannelInitializer<?>) ReflectionUtil.invoke(channelInitializerHolder, "get");
    }

    public void inject() throws Exception {
        Object connectionManager = ReflectionUtil.get(PaxyMain.PROXY, "cm", Object.class);

        Object backendInitializerHolder = ReflectionUtil.invoke(connectionManager, "getBackendChannelInitializer");
        ChannelInitializer<?> backendInitializer = getBackendInitializer();
        backendInitializerHolder.getClass().getMethod("set", ChannelInitializer.class)
                .invoke(backendInitializerHolder, new VelocityChannelInitializer(backendInitializer));
    }

}
