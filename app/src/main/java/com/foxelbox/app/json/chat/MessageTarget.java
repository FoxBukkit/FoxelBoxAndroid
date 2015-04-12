package com.foxelbox.app.json.chat;

public class MessageTarget {
    public MessageTarget(String type, String[] filter) {
        this.type = type;
        this.filter = filter;
    }

    public final String type;
    public final String[] filter;
}
