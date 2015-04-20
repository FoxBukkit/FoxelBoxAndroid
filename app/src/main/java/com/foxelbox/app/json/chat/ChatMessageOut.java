package com.foxelbox.app.json.chat;

import android.text.Spanned;
import com.foxelbox.app.util.chat.ChatFormatterUtility;

import java.util.UUID;

public class ChatMessageOut {
    private ChatMessageOut(String server, UserInfo from, String xml) {
        this.server = server;
        this.from = from;
        this.to = new MessageTarget("all", null);
        this.contents = xml;
        this.context = UUID.randomUUID();
    }

    public final String server;
    public final UserInfo from;
    public final MessageTarget to;

    public final long timestamp = System.currentTimeMillis() / 1000;

    public final UUID context;
    public final boolean finalize_context = false;
    public final String type = "text";

    public final int importance = 0;

    public final String contents;

    private transient Spanned formatted = null;

    public synchronized void formatContents() {
        if(contents == null) {
            formatted = null;
            return;
        }
        formatted = ChatFormatterUtility.formatString(contents, true);
    }

    public Spanned getFormattedContents() {
        return formatted;
    }
}