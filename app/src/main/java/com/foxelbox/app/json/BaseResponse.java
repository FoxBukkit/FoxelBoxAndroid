package com.foxelbox.app.json;

public class BaseResponse {
    public BaseResponse() { }

    public String __url;

    public String message = null;
    public boolean retry = false;
    public boolean success = false;
    public String sessionId = null;
}
