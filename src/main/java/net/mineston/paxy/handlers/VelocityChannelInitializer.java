package net.mineston.paxy.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.Method;

public class VelocityChannelInitializer extends ChannelInitializer<Channel> {
    private final ChannelInitializer<?> original;
    private final boolean clientSide;
    private static Method initChannel;

    public VelocityChannelInitializer(ChannelInitializer<?> original, boolean clientSide) {
        this.original = original;
        this.clientSide = clientSide;
    }

    static {
        try {
            initChannel = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            initChannel.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        initChannel.invoke(original, channel);
        // We need to add a separated handler because Velocity uses pipeline().get(MINECRAFT_DECODER)
        channel.pipeline().addBefore("minecraft-encoder", "via-encoder", new VelocityEncoderHandler());
        channel.pipeline().addBefore("minecraft-decoder", "via-decoder", new VelocityDecoderHandler());
    }
}