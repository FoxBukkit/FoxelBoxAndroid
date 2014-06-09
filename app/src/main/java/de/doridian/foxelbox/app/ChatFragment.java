package de.doridian.foxelbox.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatFragment extends Fragment {
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

        ((ListView)fragmentView.findViewById(R.id.listChatMessages)).setAdapter(new ArrayAdapter<String>(fragmentView.getContext(), android.R.layout.simple_list_item_1));

        chatReceiverQueue(fragmentView);

        return fragmentView;
    }

    private double lastTime = 0;

    private class ChatPollWebUtility extends WebUtility {
        private final View view;

        private ChatPollWebUtility(View view) {
            super(view.getContext());
            this.view = view;
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
            final int length = messages.length();
            for(int i = 0; i < length; i++) {
                JSONObject message = messages.getJSONObject(i);
                String messagePlain = message.getJSONObject("contents").getString("plain");
                chatMessageListAdapter.add(messagePlain);
            }

            doRun();
        }

        private void doRun() {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) { }
                    new ChatPollWebUtility(view).execute();
                }
            }.start();
        }

        @Override
        protected void onError(String message) throws JSONException {
            super.onError(message);
            doRun();
        }
    }

    public void chatReceiverQueue(View view) {
        new ChatPollWebUtility(view).doRun();
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
