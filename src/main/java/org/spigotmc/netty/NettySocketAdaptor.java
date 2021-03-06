package org.spigotmc.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

/**
 * This class wraps a Netty {@link Channel} in a {@link Socket}. It overrides
 * all methods in {@link Socket} to ensure that calls are not mistakingly made
 * to the unsupported super socket. All operations that can be sanely applied to
 * a {@link Channel} are implemented here. Those which cannot will throw an
 * {@link UnsupportedOperationException}.
 */
public class NettySocketAdaptor extends Socket {

    private final io.netty.channel.socket.SocketChannel ch;

    private NettySocketAdaptor(io.netty.channel.socket.SocketChannel ch) {
        this.ch = ch;
    }

    public static NettySocketAdaptor adapt(io.netty.channel.socket.SocketChannel ch) {
        return new NettySocketAdaptor(ch);
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        ch.bind(bindpoint).syncUninterruptibly();
    }

    @Override
    public synchronized void close() throws IOException {
        ch.close().syncUninterruptibly();
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        ch.connect(endpoint).syncUninterruptibly();
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        ch.config().setConnectTimeoutMillis(timeout);
        ch.connect(endpoint).syncUninterruptibly();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NettySocketAdaptor && ch.equals(((NettySocketAdaptor) obj).ch);
    }

    @Override
    public SocketChannel getChannel() {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public InetAddress getInetAddress() {
        return ch.remoteAddress().getAddress();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return ch.config().getOption(ChannelOption.SO_KEEPALIVE);
    }

    @Override
    public InetAddress getLocalAddress() {
        return ch.localAddress().getAddress();
    }

    @Override
    public int getLocalPort() {
        return ch.localAddress().getPort();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return ch.localAddress();
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public int getPort() {
        return ch.remoteAddress().getPort();
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return ch.config().getOption(ChannelOption.SO_RCVBUF);
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return ch.remoteAddress();
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return ch.config().getOption(ChannelOption.SO_REUSEADDR);
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return ch.config().getOption(ChannelOption.SO_SNDBUF);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return ch.config().getOption(ChannelOption.SO_LINGER);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return ch.config().getOption(ChannelOption.TCP_NODELAY);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return ch.config().getOption(ChannelOption.IP_TOS);
    }

    @Override
    public int hashCode() {
        return ch.hashCode();
    }

    @Override
    public boolean isBound() {
        return ch.localAddress() != null;
    }

    @Override
    public boolean isClosed() {
        return !ch.isOpen();
    }

    @Override
    public boolean isConnected() {
        return ch.isActive();
    }

    @Override
    public boolean isInputShutdown() {
        return ch.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return ch.isOutputShutdown();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        ch.config().setOption(ChannelOption.SO_KEEPALIVE, on);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        ch.config().setOption(ChannelOption.SO_RCVBUF, size);
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        ch.config().setOption(ChannelOption.SO_REUSEADDR, on);
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        ch.config().setOption(ChannelOption.SO_SNDBUF, size);
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        ch.config().setOption(ChannelOption.SO_LINGER, linger);
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        ch.config().setOption(ChannelOption.TCP_NODELAY, on);
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        ch.config().setOption(ChannelOption.IP_TOS, tc);
    }

    @Override
    public void shutdownInput() throws IOException {
        throw new UnsupportedOperationException("Operation not supported on Channel wrapper.");
    }

    @Override
    public void shutdownOutput() throws IOException {
        ch.shutdownOutput().syncUninterruptibly();
    }

    @Override
    public String toString() {
        return ch.toString();
    }
}
