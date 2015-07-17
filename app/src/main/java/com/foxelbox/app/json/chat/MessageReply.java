package com.foxelbox.app.json.chat;

public class MessageReply {
    public MessageReply(ChatMessageOut[] messages, Long latestId) {
        this.messages = messages;
        this.latestId = latestId;
    }

    public final ChatMessageOut[] messages;
    public final Long latestId;
}
