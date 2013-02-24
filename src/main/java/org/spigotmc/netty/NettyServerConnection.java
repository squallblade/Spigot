package org.spigotmc.netty;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PendingConnection;
import net.minecraft.server.ServerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.Spigot;

/**
 * This is the NettyServerConnection class. It implements
 * {@link ServerConnection} and is the main interface between the Minecraft
 * server and this NIO implementation. It handles starting, stopping and
 * processing the Netty backend.
 */
public class NettyServerConnection extends ServerConnection {

    private final ChannelFuture socket;
    final List<PendingConnection> pendingConnections = Collections.synchronizedList(new ArrayList<PendingConnection>());

    public NettyServerConnection(MinecraftServer ms, InetAddress host, int port) {
        super(ms);
        socket = new ServerBootstrap().channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer() {
            @Override
            public void initChannel(Channel ch) throws Exception {
                try {
                    ch.config().setOption(ChannelOption.IP_TOS, 0x18);
                } catch (ChannelException ex) {
                    // IP_TOS is not supported (Windows XP / Windows Server 2003)
                }

                ch.pipeline()
                        .addLast("timer", new ReadTimeoutHandler(30))
                        .addLast("decoder", new PacketDecoder())
                        .addLast("encoder", new PacketEncoder())
                        .addLast("manager", new NettyNetworkManager());
            }
        }).group(new NioEventLoopGroup(Spigot.nettyThreads, new ThreadFactoryBuilder().setNameFormat("Netty IO Thread - %1$d").build())).localAddress(host, port).bind();
        MinecraftServer.log.info("Using Netty NIO with {0} threads for network connections.");
    }

    /**
     * Pulse. This method pulses all connections causing them to update. It is
     * called from the main server thread a few times a tick.
     */
    @Override
    public void b() {
        super.b(); // pulse PlayerConnections
        for (int i = 0; i < pendingConnections.size(); ++i) {
            PendingConnection connection = pendingConnections.get(i);

            try {
                connection.c();
            } catch (Exception ex) {
                connection.disconnect("Internal server error");
                Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to handle packet: " + ex, ex);
            }

            if (connection.c) {
                pendingConnections.remove(i--);
            }
        }
    }

    /**
     * Shutdown. This method is called when the server is shutting down and the
     * server socket and all clients should be terminated with no further
     * action.
     */
    @Override
    public void a() {
        socket.channel().close().syncUninterruptibly();
    }

    /**
     * Return a Minecraft compatible cipher instance from the specified key.
     *
     * @param opMode the mode to initialize the cipher in
     * @param key to use as the initial vector
     * @return the initialized cipher
     */
    public static Cipher getCipher(int opMode, Key key) {
        try {
            Cipher cip = Cipher.getInstance("AES/CFB8/NoPadding");
            cip.init(opMode, key, new IvParameterSpec(key.getEncoded()));
            return cip;
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
}
