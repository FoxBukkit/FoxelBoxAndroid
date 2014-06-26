package com.foxelbox.app.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
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
    public static final String API_ENDPOINT = "http://api.foxelbox.com/v1/";

    public static class SimpleWebUtility extends WebUtility<BaseResponse> {
        public SimpleWebUtility(Activity activity) {
            super(activity);
        }

        public SimpleWebUtility(Activity activity, Context context) {
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

    protected final RT doInBackground(String url, String data) {
        try {
            return tryDownloadURLInternal(url, data);
        } catch (HttpErrorException e) {
            RT errorObject = createResponse();
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
            if(result.session_id != null && !result.session_id.isEmpty())
                LoginUtility.session_id = result.session_id;
            onSuccess(result);
        } else {
            if(result.retry && LoginUtility.enabled) {
                new LoginUtility(this, activity, context).login();
                return;
            }
            onError((result.message != null && !result.message.isEmpty()) ? result.message : "Unknown error");
        }
    }

    protected void onError(String message) {
        Log.w("foxelbox_api", message, new Throwable());
        if(context != null)
            Toast.makeText(context, "ERROR: " + message, Toast.LENGTH_SHORT).show();
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

            return new Gson().fromJson(new InputStreamReader(is), getResponseClass());
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
