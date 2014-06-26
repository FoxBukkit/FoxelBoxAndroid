package com.foxelbox.app.json;

import com.foxelbox.app.json.chat.ChatMessageOut;

public class ChatPollResponse extends BaseResponse {
    public double time;
    public ChatMessageOut[] messages;
}
