package org.spigotmc.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.DataOutputStream;
import net.minecraft.server.Packet;

/**
 * Netty encoder which takes a packet and encodes it, and adds a byte packet id
 * header.
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    private ByteBuf outBuf;
    private DataOutputStream dataOut;

    @Override
    public void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
        if (outBuf == null) {
            outBuf = ctx.alloc().directBuffer();
        }
        if (dataOut == null) {
            dataOut = new DataOutputStream(new ByteBufOutputStream(outBuf));
        }

        out.writeByte(msg.k());
        msg.a(dataOut);
        out.writeBytes(outBuf);
        out.discardSomeReadBytes();
    }

    @Override
    public void freeOutboundBuffer(ChannelHandlerContext ctx) throws Exception {
        super.freeOutboundBuffer(ctx);
        if (outBuf != null) {
            outBuf.release();
            outBuf = null;
        }
        dataOut = null;
    }
}
