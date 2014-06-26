package com.foxelbox.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.foxelbox.app.json.BaseResponse;

public class LoginUtility extends WebUtility<LoginUtility.LoginResponse> {
    public static class LoginResponse extends BaseResponse {

    }

    @Override
    public LoginResponse createResponse() {
        return new LoginResponse();
    }

    @Override
    public Class<LoginResponse> getResponseClass() {
        return LoginResponse.class;
    }

    private final WebUtility runOnSuccess;

    public static boolean enabled = false;
    protected static String session_id = null;
    public static String username = null;
    public static String password = null;

    public static boolean hasSessionId() {
        return session_id != null && !session_id.isEmpty();
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

    public LoginUtility(WebUtility runOnSuccess, Activity activity, Context context) {
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
    protected void onSuccess(LoginResponse result) {
        super.onSuccess(result);
        if(runOnSuccess != null)
            runOnSuccess.retry();
    }
}
