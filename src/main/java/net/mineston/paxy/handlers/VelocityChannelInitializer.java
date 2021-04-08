package net.mineston.paxy.handlers;

import com.github.steveice10.mc.protocol.data.SubProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.Method;

public class VelocityChannelInitializer extends ChannelInitializer<Channel> {
    private final ChannelInitializer<?> original;
    private static Method initChannel;

    public VelocityChannelInitializer(ChannelInitializer<?> original) {
        this.original = original;
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

        PaxyProtocol encoder = new PaxyProtocol(SubProtocol.HANDSHAKE, false);
        channel.pipeline().addBefore("minecraft-encoder", Handlers.ENCODER, new VelocityEncoderHandler(encoder));

        PaxyProtocol decoder = new PaxyProtocol(SubProtocol.LOGIN, true);
        channel.pipeline().addBefore("minecraft-decoder", Handlers.DECODER, new VelocityDecoderHandler(decoder));
    }
}