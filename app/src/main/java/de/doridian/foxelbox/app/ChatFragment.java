package de.doridian.foxelbox.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatFragment extends MainActivity.PlaceholderFragment {
    private static ChatPollWebUtility chatPollWebUtility = null;

    private static double lastTime = 0;
    private static ArrayList<String> messageCache = new ArrayList<String>();

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

        ArrayAdapter<String> items = new ArrayAdapter<String>(fragmentView.getContext(), android.R.layout.simple_list_item_1);
        ((ListView)fragmentView.findViewById(R.id.listChatMessages)).setAdapter(items);

        return fragmentView;
    }

    private class ChatPollWebUtility extends WebUtility {
        private final View view;
        private boolean running;

        private ChatPollWebUtility(View view) {
            super(view.getContext());
            this.view = view;
            this.running = true;
        }

        public void execute() {
            if(LoginUtility.session_id == null) {
                doRun();
                return;
            }

            execute("message/poll", WebUtility.encodeData("since", "" + lastTime));
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

            doRun();
        }

        private void doRun() {
            if(!running)
                return;
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) { }
                    stopPoll();
                    chatPollWebUtility = new ChatPollWebUtility(view);
                    chatPollWebUtility.execute();
                }
            }.start();
        }

        private void stopPoll() {
            running = false;
        }

        @Override
        protected void onError(String message) throws JSONException {
            super.onError(message);
            doRun();
        }
    }

    public void chatReceiverQueue(View view) {
        stopChatReceiverQueue();
        chatPollWebUtility = new ChatPollWebUtility(view);
        chatPollWebUtility.doRun();
    }

    public void stopChatReceiverQueue() {
        if(chatPollWebUtility != null)
            chatPollWebUtility.stopPoll();
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
            chatMessageListAdapter.addAll(messageCache);
        }
        chatReceiverQueue(getView());
    }

    public void sendChatMessage(View view) {
        EditText msgTextField = ((EditText)getView().findViewById(R.id.textChatMessage));
        final CharSequence message = msgTextField.getText();
        msgTextField.setText("");
        new WebUtility(view.getContext()) {
            @Override
            protected void onSuccess(JSONObject result) throws JSONException {
                Toast.makeText(context, "DBG SUCCESS: " + result.toString(), Toast.LENGTH_SHORT).show();
            }
        }.execute("message/send", WebUtility.encodeData("message", message));
    }
}
