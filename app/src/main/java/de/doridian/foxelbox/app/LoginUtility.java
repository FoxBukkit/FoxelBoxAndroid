package de.doridian.foxelbox.app;

import android.app.Activity;
import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginUtility extends WebUtility {
    private final WebUtility runOnSuccess;

    protected static String session_id = null;
    protected static CharSequence username = null;
    protected static CharSequence password = null;

    public LoginUtility(WebUtility runOnSuccess, Activity activity, Context context) {
        super(activity, context);
        this.runOnSuccess = runOnSuccess;
    }

    public void execute() {
        execute("login/auth", WebUtility.encodeData(false, "username", username, "password", password));
    }

    @Override
    protected void onSuccess(JSONObject result) throws JSONException {
        super.onSuccess(result);
        if(runOnSuccess != null)
            runOnSuccess.retry();
    }
}
