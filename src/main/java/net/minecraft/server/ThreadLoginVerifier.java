package net.minecraft.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLEncoder;

// CraftBukkit start
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
// CraftBukkit end

class ThreadLoginVerifier extends Thread {

    final PendingConnection pendingConnection;

    // CraftBukkit start
    CraftServer server;

    ThreadLoginVerifier(PendingConnection pendingconnection, CraftServer server) {
        super("Login Verifier Thread");
        this.server = server;
        // CraftBukkit end
        this.pendingConnection = pendingconnection;
    }

    public void run() {
        try {
            // Spigot start
            if (((CraftServer) org.bukkit.Bukkit.getServer()).ipFilter) {
                try {
                    String ip = this.pendingConnection.getSocket().getInetAddress().getHostAddress();
                    String[] split = ip.split("\\.");
                    StringBuilder lookup = new StringBuilder();
                    for (int i = split.length - 1; i >= 0; i--) {
                        lookup.append(split[i]);
                        lookup.append(".");
                    }
                    if (!ip.contains("127.0.0.1")) {
                        lookup.append("xbl.spamhaus.org.");
                        if (java.net.InetAddress.getByName(lookup.toString()) != null) {
                            this.pendingConnection.networkManager.queue(new Packet255KickDisconnect("Your IP address (" + ip + ") is flagged as unsafe by spamhaus.org/xbl"));
                            this.pendingConnection.networkManager.d();
                            this.pendingConnection.c = true;
                            return;
                        }
                    }
                } catch (Exception ex) {
                }
            }
            // Spigot end
            String s = (new BigInteger(MinecraftEncryption.a(PendingConnection.a(this.pendingConnection), PendingConnection.b(this.pendingConnection).F().getPublic(), PendingConnection.c(this.pendingConnection)))).toString(16);
            URL url = new URL("http://session.minecraft.net/game/checkserver.jsp?user=" + URLEncoder.encode(PendingConnection.d(this.pendingConnection), "UTF-8") + "&serverId=" + URLEncoder.encode(s, "UTF-8"));
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s1 = bufferedreader.readLine();

            bufferedreader.close();
            if (!"YES".equals(s1)) {
                this.pendingConnection.disconnect("Failed to verify username!");
                return;
            }

            // CraftBukkit start
            if (this.pendingConnection.getSocket() == null) {
                return;
            }

            AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(PendingConnection.d(this.pendingConnection), this.pendingConnection.getSocket().getInetAddress());
            this.server.getPluginManager().callEvent(asyncEvent);

            if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
                final PlayerPreLoginEvent event = new PlayerPreLoginEvent(PendingConnection.d(this.pendingConnection), this.pendingConnection.getSocket().getInetAddress());
                if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                    event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());
                }
                Waitable<PlayerPreLoginEvent.Result> waitable = new Waitable<PlayerPreLoginEvent.Result>() {
                    @Override
                    protected PlayerPreLoginEvent.Result evaluate() {
                        ThreadLoginVerifier.this.server.getPluginManager().callEvent(event);
                        return event.getResult();
                    }};

                PendingConnection.b(this.pendingConnection).processQueue.add(waitable);
                if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                    this.pendingConnection.disconnect(event.getKickMessage());
                    return;
                }
            } else {
                if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                    this.pendingConnection.disconnect(asyncEvent.getKickMessage());
                    return;
                }
            }
            // CraftBukkit end

            PendingConnection.a(this.pendingConnection, true);
            // CraftBukkit start
        } catch (java.io.IOException exception) {
            this.pendingConnection.disconnect("Failed to verify username, session authentication server unavailable!");
        } catch (Exception exception) {
            this.pendingConnection.disconnect("Failed to verify username!");
            server.getLogger().log(java.util.logging.Level.WARNING, "Exception verifying " + PendingConnection.d(this.pendingConnection), exception);
            // CraftBukkit end
        }
    }
}
