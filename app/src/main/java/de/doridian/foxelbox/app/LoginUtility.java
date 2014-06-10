package de.doridian.foxelbox.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginUtility extends WebUtility {
    private final WebUtility runOnSuccess;

    protected static String session_id = null;
    protected static String username = null;
    protected static String password = null;

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
        execute("login/auth", WebUtility.encodeData(false, "username", username, "password", password));
    }

    public void logout() {
        execute("login/logout", WebUtility.encodeData(true));
    }

    @Override
    protected void onSuccess(JSONObject result) throws JSONException {
        super.onSuccess(result);
        if(runOnSuccess != null)
            runOnSuccess.retry();
    }
}
