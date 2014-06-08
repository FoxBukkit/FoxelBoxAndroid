package de.doridian.foxelbox.app;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginUtility extends WebUtility {
    private final WebUtility runOnSuccess;

    protected static String session_id = null;
    protected static CharSequence username = null;
    protected static CharSequence password = null;

    public LoginUtility(WebUtility runOnSuccess, Context context) {
        super(context);
        this.runOnSuccess = runOnSuccess;
    }

    public void execute() {
        execute("login", WebUtility.encodeData(false, "username", username, "password", password));
    }

    @Override
    protected void onSuccess(JSONObject result) throws JSONException {
        session_id = result.getString("session_id");
        if(this.runOnSuccess != null)
            this.runOnSuccess.retry();
    }
}
