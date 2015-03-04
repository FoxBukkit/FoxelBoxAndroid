package com.foxelbox.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import com.foxelbox.app.json.BaseResponse;

public class LoginUtility extends WebUtility<BaseResponse> {
    @Override
    public BaseResponse createResponse() {
        return new BaseResponse();
    }

    @Override
    public Class<BaseResponse> getResponseClass() {
        return BaseResponse.class;
    }

    private final WebUtility runOnSuccess;

    public static boolean enabled = false;
    protected static String session_id = null;
    public static String username = null;
    public static String password = null;

    public static boolean hasSessionId() {
        return session_id != null && !session_id.equals("");
    }

    private static final String PREF_USERNAME = "foxelbox_username";
    private static final String PREF_PASSWORD = "foxelbox_password";

    public static void loadCredentials(Activity activity) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        username = sp.getString(PREF_USERNAME, null);
        password = sp.getString(PREF_PASSWORD, null);
    }

    public static void saveCredentials(Activity activity) {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(PREF_USERNAME, username).putString(PREF_PASSWORD, password).commit();
    }

    public LoginUtility(WebUtility runOnSuccess, ActionBarActivity activity, Context context) {
        super(activity, context);
        this.runOnSuccess = runOnSuccess;
    }

    public void login() {
        if(!enabled)
            return;
        execute("login/auth", encodeData(false, "username", username, "password", password));
    }

    public void logout() {
        enabled = false;
        execute("login/logout", encodeData(true));
        session_id = null;
    }

    @Override
    protected void onSuccess(BaseResponse result) {
        super.onSuccess(result);
        if(runOnSuccess != null)
            runOnSuccess.retry();
    }
}
