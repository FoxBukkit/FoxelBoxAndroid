package com.foxelbox.app.json;

public class BaseResponse<RT> {
    public BaseResponse() { }

    public String url;

    public String message = null;
    public int statusCode = 999;
    public boolean success = false;

    public RT result;
}
