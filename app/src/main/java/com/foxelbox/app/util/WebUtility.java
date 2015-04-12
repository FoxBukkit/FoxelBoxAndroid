package com.foxelbox.app.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.foxelbox.app.json.BaseResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public abstract class WebUtility<RT extends BaseResponse> {
    //public static final String API_ENDPOINT = "http://192.168.56.1/phpstorm/FoxelBoxAPI/public/v1/";
    public static final String API_ENDPOINT = "https://api.foxelbox.com/v1/";

    public static class SimpleWebUtility extends WebUtility<BaseResponse> {
        public SimpleWebUtility(ActionBarActivity activity) {
            super(activity);
        }

        public SimpleWebUtility(ActionBarActivity activity, Context context) {
            super(activity, context);
        }

        @Override
        public BaseResponse createResponse() {
            return new BaseResponse();
        }

        @Override
        public Class<BaseResponse> getResponseClass() {
            return BaseResponse.class;
        }
    }

    public static class HttpErrorException extends Exception {
        public HttpErrorException(String detailMessage) {
            super(detailMessage);
        }
    }

    protected final Context context;
    protected final ActionBarActivity activity;

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

    public abstract RT createResponse();
    public abstract Class<RT> getResponseClass();

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

    public WebUtility(ActionBarActivity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
    }

    public WebUtility(ActionBarActivity activity, Context context) {
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
                final RT ret = doInBackground(url, data);
                if(activity == null)
                    onPostExecute(ret);
                else
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
        if(activity == null)
            return;
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

    protected boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return networkInfo != null && networkInfo.isConnected();
    }

    protected final RT doInBackground(String url, String data) {
        try {
            return tryDownloadURLInternal(url, data);
        } catch (HttpErrorException e) {
            RT errorObject = createResponse();
            errorObject.__url = url;
            errorObject.success = false;
            errorObject.message = e.getMessage();
            return errorObject;
        }
    }

    protected final void onPostExecute(RT result) {
        if(!isLongPoll()) {
            activityCounter--;
            _invalidateViewActionsMenu();
        }

        if(result == null)
            return;

        if (result.success) {
            if(result.session_id != null && !result.session_id.equals(""))
                LoginUtility.session_id = result.session_id;
            onSuccess(result);
        } else {
            if(result.retry && LoginUtility.enabled) {
                new LoginUtility(this, activity, context).login();
                return;
            }
            onErrorInternal((result.message != null && !result.message.equals("")) ? result.message : "Unknown error", new Gson().toJson(result));
        }
    }

    protected void onErrorInternal(String message, String descr) {
        Log.w("foxelbox_api", descr, new Throwable());
        onError(message);
    }

    protected void onError(String message) {
        Log.w("foxelbox_api", message, new Throwable());
        if(context != null)
            Toast.makeText(context, "ERROR: " + message, Toast.LENGTH_SHORT).show();
    }

    public static void sendChatMessage(final ActionBarActivity activity, final View view, final CharSequence message) {
        new WebUtility.SimpleWebUtility(activity, view.getContext()) {
            @Override
            protected void onSuccess(BaseResponse result) {

            }
        }.execute("message/send", WebUtility.encodeData("message", message));
    }

    protected void onSuccess(RT result) { }

    private RT tryDownloadURLInternal(String urlStr, String data)  throws HttpErrorException {
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

            RT result = new Gson().fromJson(new InputStreamReader(is), getResponseClass());
            result.__url = urlStr;
            return result;
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
