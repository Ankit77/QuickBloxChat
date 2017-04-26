package com.indianic.qbchat.utils.chat;

import android.util.Log;

import com.indianic.qbchat.activity.QbActivity;
import com.indianic.qbchat.fragment.QbChatFragment;
import com.indianic.qbchat.utils.DialogUtils;
import com.indianic.qbchat.utils.qb.QbDialogUtils;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.listeners.QBIsTypingListener;
import com.quickblox.chat.listeners.QBMessageSentListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;

public class PrivateChatImpl extends BaseChatImpl<QBPrivateChat>
        implements QBPrivateChatManagerListener, QBMessageSentListener<QBPrivateChat>, QBIsTypingListener<QBPrivateChat> {
    private static final String TAG = PrivateChatImpl.class.getSimpleName();

    private QBPrivateChatManager qbPrivateChatManager;
    private QbChatFragment chatFragment;

    public PrivateChatImpl(QBChatMessageListener chatMessageListener, Integer opponentId, QbChatFragment chatFragment) {
        super(chatMessageListener);

        qbChat = qbPrivateChatManager.getChat(opponentId);
        if (qbChat == null) {
            qbChat = qbPrivateChatManager.createChat(opponentId, this);
        } else {
            qbChat.addMessageListener(this);
        }
        qbChat.addIsTypingListener(this);
        qbChat.addMessageSentListener(this);
        this.chatFragment = chatFragment;
    }


    @Override
    protected void initManagerIfNeed() {
        if (qbPrivateChatManager == null) {
            qbPrivateChatManager = QBChatService.getInstance().getPrivateChatManager();
            qbPrivateChatManager.addPrivateChatManagerListener(this);
        }
    }

    @Override
    public void release() {
        Log.i(TAG, "Release private chat");
        initManagerIfNeed();

        qbChat.removeMessageSentListener(this);
        qbChat.removeMessageListener(this);
        qbChat.removeIsTypingListener(this);
        qbPrivateChatManager.removePrivateChatManagerListener(this);
        chatFragment = null;
    }

    @Override
    public void stopIstyping() {
        try {
            qbChat.sendStopTypingNotification();
        } catch (Exception e) {
            Log.e("IsTypingNotification", e.getMessage());
        }
    }

    @Override
    public void sendIstyping() {
        try {
            qbChat.sendIsTypingNotification();
        } catch (Exception e) {
            Log.e("StopTypingNotification", e.getMessage());
        }

    }

    @Override
    public void chatCreated(QBPrivateChat incomingPrivateChat, boolean createdLocally) {
        Log.e(TAG, "Private chat created: " + incomingPrivateChat.getParticipant() + ", createdLocally:" + createdLocally);

        if (!createdLocally) {
            qbChat = incomingPrivateChat;
            qbChat.addMessageListener(this);
        }
    }

    @Override
    public void processMessageSent(QBPrivateChat qbPrivateChat, QBChatMessage qbChatMessage) {
        Log.e(TAG, "processMessageSent: " + qbChatMessage.getBody());
        if (chatFragment != null) {
            Log.e("Tag", qbChatMessage.getDateSent() + "");
            chatFragment.showMessage(qbChatMessage);
        }
    }

    @Override
    public void processMessageFailed(QBPrivateChat qbPrivateChat, QBChatMessage qbChatMessage) {
        Log.e(TAG, "processMessageSentFail: " + qbChatMessage.getBody());

    }


    @Override
    public void processUserIsTyping(QBPrivateChat qbPrivateChat, Integer integer) {
        Log.e(TAG, "processUserIsTyping: " + "Yes");
        if (chatFragment != null) {
            chatFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String nameLatter = QbDialogUtils.getDialogName(chatFragment.getQbDialogForchat());
                    nameLatter = nameLatter.substring(0, 1).toUpperCase() + nameLatter.substring(1);
                    ((QbActivity) chatFragment.getActivity()).setToolBarTitle(nameLatter + "\n" + "typing");
                }
            });

        }
    }

    @Override
    public void processUserStopTyping(QBPrivateChat qbPrivateChat, Integer integer) {
        Log.e(TAG, "processUserStopTyping: " + "Yes");
        if (chatFragment != null) {
            chatFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatFragment.isUserAvailable(QbDialogUtils.getOpponentIdForPrivateDialog(chatFragment.getQbDialogForchat()));
                }
            });
        }
    }


}