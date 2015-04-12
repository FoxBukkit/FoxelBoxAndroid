package com.foxelbox.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.foxelbox.app.json.ChatPollResponse;
import com.foxelbox.app.json.chat.ChatMessageOut;
import com.foxelbox.app.util.LoginUtility;
import com.foxelbox.app.util.WebUtility;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class ChatPollService extends Service {
    private static final int MAX_MESSAGES = 100;

    private double lastTime = 0;
    private final LinkedList<ChatMessageOut> messageCache = new LinkedList<>();
    private static ChatPollWebUtility chatPollWebUtility = null;
    private final Set<ChatMessageReceiver> chatReceivers = new HashSet<>();

    public void resetChatMessages() {
        lastTime = 0;
        synchronized (messageCache) {
            messageCache.clear();
        }
    }

    public void stopChatReceiverQueue() {
        chatPollWebUtility = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int res = super.onStartCommand(intent, flags, startId);
        stopChatReceiverQueue();
        chatPollWebUtility = new ChatPollWebUtility();
        chatPollWebUtility.execute();
        return res;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopChatReceiverQueue();
        resetChatMessages();
    }

    public interface ChatMessageReceiver {
        public void chatMessagesReceived(Collection<ChatMessageOut> messages);
    }

    public class ChatBinder extends Binder {
        public void addReceiver(ChatMessageReceiver receiver, boolean sendExisting) {
            synchronized (chatReceivers) {
                if(sendExisting) {
                    final LinkedList<ChatMessageOut> myMessageCache;
                    synchronized (messageCache) {
                        myMessageCache = new LinkedList<ChatMessageOut>(messageCache);
                    }
                    receiver.chatMessagesReceived(myMessageCache);
                }
                chatReceivers.add(receiver);
            }
        }

        public void removeReceiver(ChatMessageReceiver receiver) {
            synchronized (chatReceivers) {
                chatReceivers.remove(receiver);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ChatBinder();
    }

    private class ChatPollWebUtility extends WebUtility<ChatPollResponse> {
        private boolean inProgress = false;

        private ChatPollWebUtility() {
            super(null, null);
        }

        public void execute() {
            if(chatPollWebUtility != this)
                return;

            if(!LoginUtility.hasSessionId()) {
                doRun(true);
                return;
            }

            inProgress = true;
            execute("message/poll", WebUtility.encodeData("since", "" + lastTime));
        }

        @Override
        public ChatPollResponse createResponse() {
            return new ChatPollResponse();
        }

        @Override
        public Class<ChatPollResponse> getResponseClass() {
            return ChatPollResponse.class;
        }

        @Override
        public boolean isLongPoll() {
            return true;
        }

        @Override
        protected void onSuccess(ChatPollResponse result) {
            if(chatPollWebUtility != this)
                return;

            lastTime = result.time;

            synchronized (chatReceivers) {
                final LinkedList<ChatMessageOut> myMessageCache = new LinkedList<ChatMessageOut>();

                synchronized (messageCache) {
                    for(ChatMessageOut message : result.messages) {
                        message.getFormattedContents();
                        messageCache.add(message);
                        myMessageCache.add(message);
                    }

                    while(messageCache.size() > MAX_MESSAGES)
                        messageCache.removeFirst();
                }

                for (final ChatMessageReceiver chatMessageReceiver : chatReceivers) {
                    Thread t = new Thread() {
                        public void run() {
                            chatMessageReceiver.chatMessagesReceived(new LinkedList<ChatMessageOut>(myMessageCache));
                        }
                    };
                    t.setDaemon(true);
                    t.start();
                }
            }

            inProgress = false;
            doRun(false);
        }

        private void doRun(final boolean doSleep) {
            if(chatPollWebUtility != this || inProgress)
                return;
            Thread t = new Thread() {
                @Override
                public void run() {
                    if(doSleep) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            return;
                        }
                    }
                    execute();
                }
            };
            t.setDaemon(true);
            t.start();
        }

        @Override
        protected void onError(String message) {
            super.onError(message);
            inProgress = false;
            doRun(true);
        }
    }
}
