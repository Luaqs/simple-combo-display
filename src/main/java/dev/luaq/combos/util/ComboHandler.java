package dev.luaq.combos.util;

import dev.luaq.combos.Combos;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.play.server.S19PacketEntityStatus;

public class ComboHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof S19PacketEntityStatus) {
            Combos.INSTANCE.handleDamagePacket((S19PacketEntityStatus) msg);
        }

        super.channelRead(ctx, msg);
    }
}
