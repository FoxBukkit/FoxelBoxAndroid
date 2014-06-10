package de.doridian.foxelbox.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatFragment extends MainActivity.PlaceholderFragment {
    private static ChatPollWebUtility chatPollWebUtility = null;

    private static double lastTime = 0;
    private static ArrayList<String> messageCache = new ArrayList<String>();

    public static void resetChatMessages() {
        lastTime = 0;
        messageCache = new ArrayList<String>();
    }

    private boolean isRunning = true;

    public ChatFragment() { }

    public ChatFragment(int sectionNumber) {
        super(sectionNumber);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);

        Button button = (Button)fragmentView.findViewById(R.id.buttonSendChat);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChatMessage(fragmentView);
            }
        });

        ArrayAdapter<String> items = new ArrayAdapter<String>(fragmentView.getContext(), R.layout.text_view_minecraft);
        ((ListView)fragmentView.findViewById(R.id.listChatMessages)).setAdapter(items);

        return fragmentView;
    }

    private class ChatPollWebUtility extends WebUtility {
        private final View view;

        private ChatPollWebUtility(Activity activity, View view) {
            super(activity, view.getContext());
            this.view = view;
        }

        public void execute() {
            if(!isRunning || chatPollWebUtility != this)
                return;

            if(LoginUtility.hasSessionId()) {
                doRun(true);
                return;
            }

            execute("message/poll", WebUtility.encodeData("since", "" + lastTime));
        }

        @Override
        public boolean isLongPoll() {
            return true;
        }

        @Override
        protected void onSuccess(JSONObject result) throws JSONException {
            lastTime = result.getDouble("time");
            final ListView chatMessageList = (ListView)view.findViewById(R.id.listChatMessages);
            final ArrayAdapter<String> chatMessageListAdapter = (ArrayAdapter<String>)chatMessageList.getAdapter();
            final JSONArray messages = result.getJSONArray("messages");
            for(int i = messages.length() - 1; i >= 0; i--) {
                JSONObject message = messages.getJSONObject(i);
                String messagePlain = message.getJSONObject("contents").getString("plain");
                chatMessageListAdapter.add(messagePlain);
                messageCache.add(messagePlain);
            }

            doRun(false);
        }

        private void doRun(final boolean doSleep) {
            if(!isRunning || chatPollWebUtility != this)
                return;
            Thread t = new Thread() {
                @Override
                public void run() {
                    if(doSleep) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) { }
                    }
                    execute();
                }
            };
            t.setDaemon(true);
            t.start();
        }

        @Override
        protected void onError(String message) throws JSONException {
            super.onError(message);
            doRun(true);
        }
    }

    public void chatReceiverQueue(View view) {
        stopChatReceiverQueue();
        isRunning = true;
        chatPollWebUtility = new ChatPollWebUtility(getActivity(), view);
        chatPollWebUtility.execute();
    }

    public void stopChatReceiverQueue() {
        isRunning = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopChatReceiverQueue();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(messageCache != null) {
            final ListView chatMessageList = (ListView)getView().findViewById(R.id.listChatMessages);
            final ArrayAdapter<String> chatMessageListAdapter = (ArrayAdapter<String>)chatMessageList.getAdapter();
            chatMessageListAdapter.clear();
            for(String str : messageCache)
                chatMessageListAdapter.add(str);
        }
        chatReceiverQueue(getView());
    }

    public void sendChatMessage(View view) {
        EditText msgTextField = ((EditText)getView().findViewById(R.id.textChatMessage));
        final CharSequence message = msgTextField.getText();
        msgTextField.setText("");
        new WebUtility(getActivity(), view.getContext()) {
            @Override
            protected void onSuccess(JSONObject result) throws JSONException {

            }
        }.execute("message/send", WebUtility.encodeData("message", message));
    }
}
