package com.foxelbox.app.gui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.foxelbox.app.R;
import com.foxelbox.app.json.chat.ChatMessageOut;
import com.foxelbox.app.service.ChatPollService;
import com.foxelbox.app.util.WebUtility;

import java.util.Collection;

public class ChatFragment extends MainActivity.PlaceholderFragment {
    private static final int MAX_MESSAGES = 1000;

    private ChatPollService.ChatBinder chatBinder = null;

    private final ChatPollService.ChatMessageReceiver chatMessageReceiver = new ChatPollService.ChatMessageReceiver() {
        @Override
        public void chatMessagesReceived(final Collection<ChatMessageOut> messages) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final View view = getView();
                    if (view == null) {
                        return;
                    }
                    final ListView chatMessageList = (ListView) view.findViewById(R.id.listChatMessages);
                    if (chatMessageList == null) {
                        return;
                    }
                    final ArrayAdapter<Spanned> chatMessageListAdapter = (ArrayAdapter<Spanned>) chatMessageList.getAdapter();
                    for (ChatMessageOut message : messages) {
                        if (message.type.equals("text")) {
                            Spanned content = message.getFormattedContents();
                            if (content != null) {
                                chatMessageListAdapter.add(content);
                            }
                        }
                    }
                    while (chatMessageListAdapter.getCount() > MAX_MESSAGES) {
                        chatMessageListAdapter.remove(chatMessageListAdapter.getItem(0));
                    }
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

        ArrayAdapter<Spanned> items = new ArrayAdapter<>(fragmentView.getContext(), R.layout.list_item_chat);
        ((ListView)fragmentView.findViewById(R.id.listChatMessages)).setAdapter(items);

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        final ListView chatMessageList = (ListView)getView().findViewById(R.id.listChatMessages);
        final ArrayAdapter<Spanned> chatMessageListAdapter = (ArrayAdapter<Spanned>)chatMessageList.getAdapter();
        chatMessageListAdapter.clear();
        getActivity().bindService(new Intent(getActivity(), ChatPollService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(chatBinder != null) {
            chatBinder.removeReceiver(chatMessageReceiver);
        }
        getActivity().unbindService(serviceConnection);
    }

    private void sendChatMessage(View view) {
        EditText msgTextField = ((EditText)view.findViewById(R.id.textChatMessage));
        final CharSequence message = msgTextField.getText();
        msgTextField.setText("");
        WebUtility.sendChatMessage(getActionBarActivity(), view, message);
    }
}
