package org.spigotmc.netty;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import net.minecraft.server.Connection;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.Packet;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * This class is used for plugins that wish to register to listen to incoming
 * and outgoing packets. To use this class, simply create a new instance,
 * override the methods you wish to use, and call
 * {@link #register(org.spigotmc.netty.PacketListener, org.bukkit.plugin.Plugin)}.
 */
public class PacketListener {

    /**
     * A mapping of all registered listeners and their owning plugins.
     */
    private static final Map<PacketListener, Plugin> listeners = new HashMap<PacketListener, Plugin>();
    /**
     * A baked list of all listeners, for efficiency sake.
     */
    private static PacketListener[] baked = new PacketListener[0];

    /**
     * Used to register a handler for receiving notifications of packet
     * activity.
     *
     * @param listener the listener to register
     * @param plugin the plugin owning this listener
     */
    public static synchronized void register(PacketListener listener, Plugin plugin) {
        Preconditions.checkNotNull(listener, "listener");
        Preconditions.checkNotNull(plugin, "plugin");
        Preconditions.checkState(!listeners.containsKey(listener), "listener already registered");

        int size = listeners.size();
        Preconditions.checkState(baked.length == size);
        listeners.put(listener, plugin);
        baked = Arrays.copyOf(baked, size + 1);
        baked[size] = listener;
    }

    static Packet callReceived(INetworkManager networkManager, Connection connection, Packet packet) {
        for (PacketListener listener : baked) {
            try {
                packet = listener.packetReceived(networkManager, connection, packet);
            } catch (Throwable t) {
                Bukkit.getServer().getLogger().log(Level.SEVERE, "Error whilst firing receive hook for packet", t);
            }
        }
        return packet;
    }

    static Packet callQueued(INetworkManager networkManager, Connection connection, Packet packet) {
        for (PacketListener listener : baked) {
            try {
                packet = listener.packetQueued(networkManager, connection, packet);
            } catch (Throwable t) {
                Bukkit.getServer().getLogger().log(Level.SEVERE, "Error whilst firing queued hook for packet", t);
            }
        }
        return packet;
    }

    /**
     * Called when a packet has been received and is about to be handled by the
     * current {@link Connection}. The returned packet will be the packet passed
     * on for handling, or in the case of null being returned, not handled at
     * all.
     *
     * @param networkManager the NetworkManager receiving the packet
     * @param connection the connection which will handle the packet
     * @param packet the received packet
     * @return the packet to be handled, or null to cancel
     */
    public Packet packetReceived(INetworkManager networkManager, Connection connection, Packet packet) {
        return packet;
    }

    /**
     * Called when a packet is queued to be sent. The returned packet will be
     * the packet sent. In the case of null being returned, the packet will not
     * be sent.
     *
     * @param networkManager the NetworkManager which will send the packet
     * @param connection the connection which queued the packet
     * @param packet the queue packet
     * @return the packet to be sent, or null if the packet will not be sent.
     */
    public Packet packetQueued(INetworkManager networkManager, Connection connection, Packet packet) {
        return packet;
    }
}
