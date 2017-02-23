package me.imTedzi.ABA.spigot.util;

import java.util.UUID;

/**
 * Represents a client connecting to the server.
 *
 */
public class LoginSession {

    private final String username;

    private UUID uuid;

    public LoginSession(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Get the premium UUID of this player
     *
     * @return the premium UUID or null if not fetched
     */
    public synchronized UUID getUuid() {
        return uuid;
    }

    /**
     * Set the online UUID if it's fetched
     *
     * @param uuid premium UUID
     */
    public synchronized void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
