package com.foxelbox.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import com.foxelbox.app.json.BaseResponse;
import com.foxelbox.app.json.player.login.LoginResponseData;
import com.google.gson.reflect.TypeToken;

public class LoginUtility extends WebUtility<LoginResponseData> {
    private final WebUtility runOnSuccess;

    public static boolean enabled = false;
    static String sessionId = null;
    public static String username = null;
    public static String password = null;

    public static boolean hasSessionId() {
        return sessionId != null && !sessionId.equals("");
    }

    private static final String PREF_USERNAME = "foxelbox_username";
    private static final String PREF_PASSWORD = "foxelbox_password";

    @Override
    protected TypeToken<BaseResponse<LoginResponseData>> getTypeToken() {
        return new TypeToken<BaseResponse<LoginResponseData>>(){};
    }

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

    @Override
    protected boolean canRetry() {
        return false;
    }

    public void login() {
        if(!enabled)
            return;
        execute("POST", "login", encodeData("username", username, "password", password), false);
    }

    @Override
    protected void onSuccess(LoginResponseData result) {
        super.onSuccess(result);
        sessionId = result.sessionId;
        if(runOnSuccess != null)
            runOnSuccess.retry();
    }
}
