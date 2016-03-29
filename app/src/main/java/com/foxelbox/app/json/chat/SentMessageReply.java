package com.foxelbox.app.json.chat;

import java.util.UUID;

public class SentMessageReply {
    public SentMessageReply(UUID context) {
        this.context = context;
    }

    public final UUID context;
}
