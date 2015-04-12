package com.foxelbox.app.json.chat;

import java.util.UUID;

public class UserInfo {
    public UserInfo(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public final UUID uuid;
    public final String name;
}
