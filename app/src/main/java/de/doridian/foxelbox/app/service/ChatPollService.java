package de.doridian.foxelbox.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.Spannable;
import de.doridian.foxelbox.app.gui.ChatFormatterUtility;
import de.doridian.foxelbox.app.util.LoginUtility;
import de.doridian.foxelbox.app.util.WebUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class ChatPollService extends Service {
    private static final int MAX_MESSAGES = 100;

    private double lastTime = 0;
    private final ArrayList<Spannable> messageCache = new ArrayList<Spannable>();
    private static ChatPollWebUtility chatPollWebUtility = null;
    private final Set<ChatMessageReceiver> chatReceivers = Collections.newSetFromMap(new HashMap<ChatMessageReceiver, Boolean>());

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
        public void chatMessagesReceived(Collection<Spannable> messages);
    }

    public class ChatBinder extends Binder {
        public void addReceiver(ChatMessageReceiver receiver, boolean sendExisting) {
            synchronized (chatReceivers) {
                if(sendExisting) {
                    final ArrayList<Spannable> myMessageCache;
                    synchronized (messageCache) {
                        myMessageCache = new ArrayList<Spannable>(messageCache);
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

    private class ChatPollWebUtility extends WebUtility {
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
        public boolean isLongPoll() {
            return true;
        }

        @Override
        protected void onSuccess(JSONObject result) throws JSONException {
            if(chatPollWebUtility != this)
                return;

            lastTime = result.getDouble("time");
            final JSONArray messages = result.getJSONArray("messages");
            synchronized (chatReceivers) {
                final ArrayList<Spannable> myMessageCache = new ArrayList<Spannable>();

                synchronized (messageCache) {
                    for (int i = messages.length() - 1; i >= 0; i--) {
                        JSONObject message = messages.getJSONObject(i);
                        String messagePlain = message.getJSONObject("contents").getString("plain");
                        Spannable messageFormatted = ChatFormatterUtility.formatString(messagePlain);
                        messageCache.add(messageFormatted);
                        myMessageCache.add(messageFormatted);
                    }

                    while(messageCache.size() > MAX_MESSAGES)
                        messageCache.remove(0);
                }

                for (final ChatMessageReceiver chatMessageReceiver : chatReceivers) {
                    Thread t = new Thread() {
                        public void run() {
                            chatMessageReceiver.chatMessagesReceived(new ArrayDeque<Spannable>(myMessageCache));
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
        protected void onError(String message) throws JSONException {
            super.onError(message);
            inProgress = false;
            doRun(true);
        }
    }
}
