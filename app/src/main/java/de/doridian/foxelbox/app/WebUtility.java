package de.doridian.foxelbox.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class WebUtility {
    public static final String API_ENDPOINT = "http://192.168.56.1/phpstorm/FoxelBoxAPI/public/v1/";
    //public static final String API_ENDPOINT = "http://api.foxelbox.com/v1/";

    public static class HttpErrorException extends Exception {
        public HttpErrorException(String detailMessage) {
            super(detailMessage);
        }
    }

    protected final Context context;
    protected final Activity activity;

    private static volatile int activityCounter = 0;
    public static boolean isRunning() {
        return activityCounter > 0;
    }

    private static String urlEncode(CharSequence str) {
        try {
            return URLEncoder.encode(str.toString(), "UTF-8");
        } catch (Exception e) {
            Log.e("foxelbox", e.getMessage(), e);
            return null;
        }
    }

    public static String encodeData(CharSequence... dataVararg) {
        return encodeData(true, dataVararg);
    }

    protected static String encodeData(boolean add_session_id, CharSequence... dataVararg) {
        Map<CharSequence, CharSequence> data = new HashMap<CharSequence, CharSequence>();
        for(int i = 0; i < dataVararg.length; i += 2) {
            data.put(dataVararg[i], dataVararg[i + 1]);
        }
        return encodeData(data, add_session_id);
    }

    public static String encodeData(Map<CharSequence, CharSequence> data) {
        return encodeData(data, true);
    }

    protected static String encodeData(Map<CharSequence, CharSequence> data, boolean add_session_id) {
        if(add_session_id)
            data.put("session_id", LoginUtility.session_id);
        StringBuilder result = new StringBuilder();
        boolean notFirst = false;
        for(Map.Entry<CharSequence, CharSequence> entries : data.entrySet()) {
            if(notFirst)
                result.append('&');
            else
                notFirst = true;
            result.append(urlEncode(entries.getKey()));
            result.append('=');
            result.append(urlEncode(entries.getValue()));
        }
        return result.toString();
    }

    public WebUtility(Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
    }

    public WebUtility(Activity activity, Context context) {
        this.context = context;
        this.activity = activity;
    }

    private String lastURL;
    private String lastData;

    public void retry() {
        execute(lastURL, lastData);
    }

    public boolean isLongPoll() {
        return false;
    }

    public void execute(final String url, final String data) {
        lastURL = url;
        lastData = data;
        onPreExecute();
        Thread t = new Thread() {
            @Override
            public void run() {
                final JSONObject ret = doInBackground(url, data);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute(ret);
                    }
                });
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private void _invalidateViewActionsMenu() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.invalidateOptionsMenu();
            }
        });
    }

    protected void onPreExecute() {
        if(!isLongPoll()) {
            activityCounter++;
            _invalidateViewActionsMenu();
        }
    }

    protected final JSONObject doInBackground(String url, String data) {
        try {
            return tryDownloadURLInternal(url, data);
        } catch (HttpErrorException e) {
            try {
                JSONObject errorObject = new JSONObject();
                errorObject.put("success", false);
                errorObject.put("message", e.getMessage());
                return errorObject;
            } catch (JSONException je) {
                Log.e("foxelbox", je.getMessage(), je);
                return null;
            }
        }
    }

    protected final void onPostExecute(JSONObject result) {
        if(!isLongPoll()) {
            activityCounter--;
            _invalidateViewActionsMenu();
        }

        if(result == null)
            return;

        try {
            if (result.has("success") && result.getBoolean("success")) {
                if(result.has("session_id"))
                    LoginUtility.session_id = result.getString("session_id");
                onSuccess(result.getJSONObject("result"));
            } else {
                if(result.has("retry") && result.getBoolean("retry")) {
                    new LoginUtility(this, activity, context).execute();
                    return;
                }
                onError(result.has("message") ? result.getString("message") : "Unknown error");
            }
        } catch (JSONException e) {
            Log.e("foxelbox", e.getMessage(), e);
        }
    }

    protected void onError(String message) throws JSONException {
        Toast.makeText(context, "ERROR: " + message, Toast.LENGTH_SHORT).show();
    }

    protected void onSuccess(JSONObject result) throws JSONException { }

    private JSONObject tryDownloadURLInternal(String urlStr, String data)  throws HttpErrorException {
        InputStream is = null;

        try {
            URL url = new URL(API_ENDPOINT + urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(isLongPoll() ? 60000 : 5000 /* milliseconds */);
            conn.setConnectTimeout(5000 /* milliseconds */);
            conn.setDoInput(true);
            if(data != null) {
                if(isLongPoll())
                    data += "&longpoll=true";
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes("UTF-8"));
                os.close();
            } else {
                conn.setRequestMethod("GET");
            }
            // Starts the query
            int response = conn.getResponseCode();
            if(response != 200)
                throw new HttpErrorException("Response code " + response);
            is = conn.getInputStream();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] strBuffer = new byte[4096]; int len;
            while((len = is.read(strBuffer)) > 0)
                byteArrayOutputStream.write(strBuffer, 0, len);

            return (JSONObject)new JSONTokener(new String(byteArrayOutputStream.toByteArray(), "UTF-8")).nextValue();
        } catch (Exception e) {
            throw new HttpErrorException(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) { }
            }
        }
    }
}
