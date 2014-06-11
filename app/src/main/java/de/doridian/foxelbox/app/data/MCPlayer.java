package de.doridian.foxelbox.app.data;

public class MCPlayer {
    private final String uuid;
    private String displayName;
    private String name;

    public MCPlayer(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
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
