package com.indianic.qbchat.utils.chat;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.listeners.QBMessageSentListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

public class GroupChatImpl extends BaseChatImpl<QBGroupChat> implements QBMessageSentListener<QBGroupChat> {
    private static final String TAG = GroupChatImpl.class.getSimpleName();

    private QBGroupChatManager qbGroupChatManager;

    public GroupChatImpl(QBChatMessageListener chatMessageListener) {
        super(chatMessageListener);

    }

    @Override
    protected void initManagerIfNeed() {
        if (qbGroupChatManager == null) {
            qbGroupChatManager = QBChatService.getInstance().getGroupChatManager();
        }
    }

    public void joinGroupChat(QBDialog dialog, QBEntityCallback<Void> callback) {
        initManagerIfNeed();
        if (qbChat == null) {
            qbChat = qbGroupChatManager.createGroupChat(dialog.getRoomJid());
        }
        join(callback);
    }

    private void join(final QBEntityCallback<Void> callback) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        qbChat.join(history, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(final Void result, final Bundle bundle) {
                qbChat.addMessageListener(GroupChatImpl.this);
                qbChat.addMessageSentListener(GroupChatImpl.this);

                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callback.onSuccess(result, bundle);
                        }
                        catch (Exception e)
                        {}
                    }
                });
                Log.i(TAG, "Join successful");
            }

            @Override
            public void onError(final QBResponseException e) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callback.onError(e);
                        } catch (Exception e1) {
                        }
                    }
                });
            }
        });
    }

    public void leaveChatRoom() {
        try {
            qbChat.leave();
        } catch (SmackException.NotConnectedException | XMPPException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() throws XMPPException {
        if (qbChat != null) {
            leaveChatRoom();
            qbChat.removeMessageListener(this);
        }
    }

    @Override
    public void stopIstyping() {

    }

    @Override
    public void sendIstyping() {

    }

    @Override
    public void processMessageSent(QBGroupChat qbGroupChat, QBChatMessage qbChatMessage) {

    }

    @Override
    public void processMessageFailed(QBGroupChat qbGroupChat, QBChatMessage qbChatMessage) {

    }
}
