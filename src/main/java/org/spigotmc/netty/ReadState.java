package org.spigotmc.netty;

/**
 * Stores the state of the packet currently being read.
 */
public enum ReadState {

    /**
     * Indicates the byte representing the ID has been read.
     */
    HEADER,
    /**
     * Shows the packet body is being read.
     */
    DATA;
}
