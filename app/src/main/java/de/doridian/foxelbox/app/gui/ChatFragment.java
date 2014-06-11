package de.doridian.foxelbox.app.gui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import de.doridian.foxelbox.app.R;
import de.doridian.foxelbox.app.service.ChatPollService;
import de.doridian.foxelbox.app.util.WebUtility;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

public class ChatFragment extends MainActivity.PlaceholderFragment {
    public ChatFragment() { }

    public ChatFragment(int sectionNumber) {
        super(sectionNumber);
    }

    private ChatPollService.ChatBinder chatBinder = null;

    private final ChatPollService.ChatMessageReceiver chatMessageReceiver = new ChatPollService.ChatMessageReceiver() {
        @Override
        public void chatMessagesReceived(final Collection<Spannable> messages) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ListView chatMessageList = (ListView)getView().findViewById(R.id.listChatMessages);
                    final ArrayAdapter<Spannable> chatMessageListAdapter = (ArrayAdapter<Spannable>)chatMessageList.getAdapter();
                    chatMessageListAdapter.addAll(messages);
                    while(chatMessageListAdapter.getCount() > ChatPollService.MAX_MESSAGES)
                        chatMessageListAdapter.remove(chatMessageListAdapter.getItem(0));
                }
            });
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            chatBinder = (ChatPollService.ChatBinder)iBinder;
            chatBinder.addReceiver(chatMessageReceiver, true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if(chatBinder != null)
                chatBinder.removeReceiver(chatMessageReceiver);
        }
    };

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

        ArrayAdapter<Spannable> items = new ArrayAdapter<Spannable>(fragmentView.getContext(), R.layout.list_item_small);
        ((ListView)fragmentView.findViewById(R.id.listChatMessages)).setAdapter(items);

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        final ListView chatMessageList = (ListView)getView().findViewById(R.id.listChatMessages);
        final ArrayAdapter<Spannable> chatMessageListAdapter = (ArrayAdapter<Spannable>)chatMessageList.getAdapter();
        chatMessageListAdapter.clear();
        getActivity().bindService(new Intent(getActivity(), ChatPollService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(chatBinder != null)
            chatBinder.removeReceiver(chatMessageReceiver);
        getActivity().unbindService(serviceConnection);
    }

    public void sendChatMessage(View view) {
        EditText msgTextField = ((EditText)view.findViewById(R.id.textChatMessage));
        final CharSequence message = msgTextField.getText();
        msgTextField.setText("");
        new WebUtility(getActivity(), view.getContext()) {
            @Override
            protected void onSuccess(JSONObject result) throws JSONException {

            }
        }.execute("message/send", WebUtility.encodeData("message", message));
    }
}
