package com.foxelbox.app.data;

import java.util.UUID;

public class MCPlayer {
    private final UUID uuid;
    private String displayName;
    private String name;

    public MCPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
