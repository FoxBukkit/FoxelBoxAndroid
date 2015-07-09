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
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class WebUtility<RT> {
    public static final String API_ENDPOINT = "https://api.foxelbox.com/v2/";

    public static class SimpleWebUtility extends WebUtility<String> {
        public SimpleWebUtility(ActionBarActivity activity) {
            super(activity);
        }

        public SimpleWebUtility(ActionBarActivity activity, Context context) {
            super(activity, context);
        }

        @Override
        protected TypeToken<BaseResponse<String>> getTypeToken() {
            return new TypeToken<BaseResponse<String>>(){};
        }
    }

    public static class HttpErrorException extends Exception {
        public final int responseCode;
        public HttpErrorException(String detailMessage, int responseCode) {
            super(detailMessage);
            this.responseCode = responseCode;
        }
    }

    protected boolean canRetry() {
        return true;
    }

    protected boolean mayRefreshToken() {
        return true;
    }

    protected final Context context;
    protected final ActionBarActivity activity;

    private static volatile int activityCounter = 0;
    public static boolean isRunning() {
        return activityCounter > 0;
    }

    protected abstract TypeToken<BaseResponse<RT>> getTypeToken();

    private static String urlEncode(CharSequence str) {
        try {
            return URLEncoder.encode(str.toString(), "UTF-8");
        } catch (Exception e) {
            Log.e("foxelbox", e.getMessage(), e);
            return null;
        }
    }

    public static String encodeData(CharSequence... dataVararg) {
        Map<CharSequence, CharSequence> data = new HashMap<>();
        for(int i = 0; i < dataVararg.length; i += 2) {
            data.put(dataVararg[i], dataVararg[i + 1]);
        }
        return encodeData(data);
    }

    public static String encodeData(Map<CharSequence, CharSequence> data) {
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
    private String lastMethod;
    private String lastData;
    private boolean lastAddSessionId;

    public void retry() {
        execute(lastMethod, lastURL, lastData, lastAddSessionId);
    }

    public boolean isLongPoll() {
        return false;
    }

    public void execute(final String method, final String url) {
        execute(method, url, null);
    }

    public void execute(final String method, final String url, final String data) {
        execute(method, url, data, true);
    }

    public void execute(final String method, final String url, final String data, final boolean addSessionId) {
        lastURL = url;
        lastData = data;
        lastMethod = method;
        lastAddSessionId = addSessionId;
        if(addSessionId && mayRefreshToken()) {
            long timeUntilExpiry = LoginUtility.expiresAt - ((new Date()).getTime() / 1000L);
            if(timeUntilExpiry < 1) {
                new LoginUtility(this, activity, context).login();
                return;
            } else if(timeUntilExpiry < 60) {
                new LoginUtility(this, activity, context).refresh();
                return;
            }
        }
        final String sessionId = addSessionId ? LoginUtility.sessionId : null;
        onPreExecute();
        Thread t = new Thread() {
            @Override
            public void run() {
                final BaseResponse<RT> ret = doInBackground(method, url, data, sessionId);
                if(activity == null) {
                    onPostExecute(ret);
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onPostExecute(ret);
                        }
                    });
                }
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

    protected final BaseResponse<RT> doInBackground(String method, String url, String data, String sessionId) {
        try {
            return tryDownloadURLInternal(method, url, data, sessionId);
        } catch (HttpErrorException e) {
            BaseResponse<RT> errorObject = new BaseResponse<>();
            errorObject.url = url;
            errorObject.statusCode = e.responseCode;
            errorObject.success = false;
            errorObject.message = e.getMessage();
            return errorObject;
        }
    }

    protected final void onPostExecute(BaseResponse<RT> result) {
        if(!isLongPoll()) {
            activityCounter--;
            _invalidateViewActionsMenu();
        }

        if(result == null)
            return;

        if (result.success) {
            onSuccess(result.result);
        } else {
            if(canRetry() && (result.statusCode == 401 || result.statusCode == 403) && LoginUtility.enabled) {
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
        new WebUtility.SimpleWebUtility(activity, view.getContext()).execute("POST", "message", WebUtility.encodeData("message", message));
    }

    protected void onSuccess(RT result) { }

    private BaseResponse<RT> tryDownloadURLInternal(String method, String urlStr, String data, String sessionId)  throws HttpErrorException {
        InputStream is = null;

        try {
            final String useUrl;
            boolean dataInBody = false;
            if (isLongPoll()) {
                if(data == null) {
                    data = "longPoll=true";
                } else {
                    data += "&longPoll=true";
                }
            }
            if (data != null) {
                if(method.equalsIgnoreCase("put") || method.equalsIgnoreCase("post")) {
                    dataInBody = true;
                    useUrl = urlStr;
                } else {
                    useUrl = urlStr + "?" + data;
                }
            } else {
                useUrl = urlStr;
            }
            URL url = new URL(API_ENDPOINT + useUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(isLongPoll() ? 60000 : 5000 /* milliseconds */);
            conn.setConnectTimeout(5000 /* milliseconds */);
            conn.setDoInput(true);
            if (sessionId != null) {
                conn.setRequestProperty("Authorization", sessionId);
            }
            if (dataInBody) {
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes("UTF-8"));
                os.close();
            }
            conn.setRequestMethod(method);
            // Starts the query
            int response = conn.getResponseCode();
            if(response >= 200 && response <= 299) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            BaseResponse<RT> result = new Gson().fromJson(new InputStreamReader(is), getTypeToken().getType());
            result.url = urlStr;
            result.statusCode = response;
            return result;
        } catch (Exception e) {
            throw new HttpErrorException(e.getMessage(), 509);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) { }
            }
        }
    }
}
