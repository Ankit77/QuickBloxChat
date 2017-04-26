package com.indianic.qbchat.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.R;
import com.indianic.qbchat.activity.QbActivity;
import com.indianic.qbchat.adapter.QbChatMessageAdapter;
import com.indianic.qbchat.commanclass.QbBroadcastSend;
import com.indianic.qbchat.utils.ConnectivityUtils;
import com.indianic.qbchat.utils.ErrorUtils;
import com.indianic.qbchat.utils.Toaster;
import com.indianic.qbchat.utils.chat.Chat;
import com.indianic.qbchat.utils.chat.ChatHelper;
import com.indianic.qbchat.utils.chat.GroupChatImpl;
import com.indianic.qbchat.utils.chat.PrivateChatImpl;
import com.indianic.qbchat.utils.chat.QBChatMessageListener;
import com.indianic.qbchat.utils.qb.QbDialogUtils;
import com.indianic.qbchat.utils.qb.VerboseQbChatConnectionListener;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Purpose: This class to setup basic requirement for one to one and group chat initialized.
 * Created by PG on 24/05/16.
 */
public class QbChatFragment extends QbBaseFragment implements TextWatcher {
    private static final String TAG = QbChatFragment.class.getSimpleName();
    public static final String EXTRA_DIALOG = "dialog";
    private static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    private int skipPagination = 0;
    private View mainView;
    private Chat chat;
    private StickyListHeadersListView messagesListView;
    private EditText messageEditText;
    private ImageButton btn_send;
    private Snackbar snackbar;
    private ConnectionListener chatConnectionListener;
    private ArrayList<QBChatMessage> unShownMessages;
    private QbChatMessageAdapter chatAdapter;
    private QBDialog qbDialogForchat;
    private ProgressBar progressBarMessage;
    private QBRosterListener qbRosterListener;
    private QBRoster chatRoster;
    private String Isonline = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.qbfragment_chat, container, false);
    }

    @Override
    public void initView(View view) {
        ((QbActivity)getActivity()).removeUserListFragment();
        qbDialogForchat = (QBDialog) this.getArguments().getSerializable(EXTRA_DIALOG);
        unShownMessages = new ArrayList<QBChatMessage>();
        progressBarMessage = _findViewById(R.id.progress_messageload, view);
        mainView = _findViewById(R.id.activity_chat_relativelayoutMain, view);
        messagesListView = _findViewById(R.id.list_chat_messages, view);
        messageEditText = _findViewById(R.id.edit_chat_message, view);
        btn_send = _findViewById(R.id.button_chat_send, view);
        btn_send.setOnClickListener(this);
        chatAdapter = new QbChatMessageAdapter(getActivity(), unShownMessages);
        messagesListView.setAdapter(chatAdapter);
        messagesListView.setAreHeadersSticky(false);
        messagesListView.setDivider(null);
        if (qbDialogForchat.getType() == QBDialogType.PRIVATE) {
            messageEditText.addTextChangedListener(this);
        }
        initChatConnectionListener();
        initChat();

        // ((QbActivity) getActivity()).setToolBarTitle(nameLatter);

    }

    @Override
    public void initActionBar() {

    }

    /**
     * Method used for geting Current chat dialog
     * @return
     */
    public QBDialog getQbDialogForchat() {
        return qbDialogForchat;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_chat_send) {
            String text = messageEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendChatMessage(text);
            }

        }
    }

    /**
     * Method used for initialize all chat component
     */
    private void initChat() {
        switch (qbDialogForchat.getType()) {
            case PRIVATE:
                chat = new PrivateChatImpl(chatMessageListener, QbDialogUtils.getOpponentIdForPrivateDialog(qbDialogForchat), this);
                try {
                    ChatHelper.getInstance().addtoRoaster(QbDialogUtils.getOpponentIdForPrivateDialog(qbDialogForchat));
                    isUserAvailable(QbDialogUtils.getOpponentIdForPrivateDialog(qbDialogForchat));
                    roasterList();
                } catch (Exception e) {
                    Log.e("RoasterEntryError", e.getMessage());
                }
                if (ConnectivityUtils.isNetworkConnected()) {
                    loadChatHistory();
                } else {
                    snackbar = ErrorUtils.showSnackbar(mainView, R.string.no_internet_connection, R.string.dlg_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            loadChatHistory();
                        }
                    });
                }
                break;
            case GROUP:
                chat = new GroupChatImpl(chatMessageListener);
                joinGroupChat();
                break;
            default:
                // Toaster.shortToast(String.format("%s %s", getString(R.string.chat_unsupported_type), qbDialog.getType().name()));
                //finish();
                break;
        }
    }

    /**
     * Method used for user add to roaster for geting online or offline status
     */
    public void roasterList() {
        qbRosterListener = new QBRosterListener() {
            @Override
            public void entriesDeleted(Collection<Integer> userIds) {
                Log.e("Roaster", "Delete roaster");
            }

            @Override
            public void entriesAdded(Collection<Integer> userIds) {
                Log.e("Roaster", "Addedto roaster 11111");
            }

            @Override
            public void entriesUpdated(Collection<Integer> userIds) {
                Log.e("Roaster", "Update roaster");
            }

            @Override
            public void presenceChanged(QBPresence presence) {
                Log.e("Roaster", "Presence change roaster Chat screennnnnnnn" + presence.getUserId() + "==" + presence.getType());
                if (presence.getUserId().toString().equalsIgnoreCase(QbDialogUtils.getOpponentIdForPrivateDialog(qbDialogForchat).toString())) {
                    actionbarchange(presence);
                }
                //isUserAvailable(presence.getUserId());


            }
        };

        QBSubscriptionListener subscriptionListener = new QBSubscriptionListener() {
            @Override
            public void subscriptionRequested(int userId) {
                // addtoRoaster(userId);
                Log.e("RoasterChatService", "subbbbbbbb roaster=====" + userId);
            }
        };


        // Do this after success Chat login
        chatRoster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual, subscriptionListener);
        chatRoster.addRosterListener(qbRosterListener);

    }

    /**
     * Method used for joined user to group for chat
     */
    private void joinGroupChat() {
        ((GroupChatImpl) chat).joinGroupChat(qbDialogForchat, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {

                if (ConnectivityUtils.isNetworkConnected()) {
                    loadChatHistory();
                } else {
                    snackbar = ErrorUtils.showSnackbar(mainView, R.string.no_internet_connection, R.string.dlg_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                            loadChatHistory();
                        }
                    });
                }
            }

            @Override
            public void onError(QBResponseException list) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setMessage("error when join group chat: " + list.toString()).create().show();
            }
        });
    }

    /**
     * Method user for connect xmpp conection to quickblox server
     */
    private void initChatConnectionListener() {
        Log.e(TAG, "xmpp Into Init Connection");
        chatConnectionListener = new VerboseQbChatConnectionListener(mainView) {
            @Override
            public void connectionClosedOnError(final Exception e) {
                Log.e(TAG, "xmpp connectionClosedOnError");
                super.connectionClosedOnError(e);
                QbChatFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toaster.shortToast("ConectionClosedError");
                    }
                });

            }

            @Override
            public void connectionClosed() {
                Log.e(TAG, "xmpp connectionClosed");
                QbChatFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toaster.shortToast("ConectionClosed");
                    }
                });
            }

            @Override
            public void reconnectionFailed(Exception error) {
                super.reconnectionFailed(error);
                QbChatFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toaster.shortToast("ConectionFail");
                    }
                });
            }

            @Override
            public void reconnectionSuccessful() {
                super.reconnectionSuccessful();
                skipPagination = 0;
                chatAdapter = null;
                switch (qbDialogForchat.getType()) {
                    case PRIVATE:
                        QbChatFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toaster.shortToast("Reconected");
                                loadChatHistory();
                            }
                        });
                        break;
                }
            }

            @Override
            public void connected(XMPPConnection connection) {
                super.connected(connection);
                Log.e(TAG, "xmpp Connected");
                QbChatFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toaster.shortToast("Conected");
                        loadChatHistory();
                    }
                });
            }
        };
        ChatHelper.getInstance().addConnectionListener(chatConnectionListener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Destroy========>Yes");
        ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);
        try {
            if (qbDialogForchat.getType() == QBDialogType.PRIVATE) {
                chatRoster.removeRosterListener(qbRosterListener);
                chatRoster = null;
            }
            chat.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        chat = null;
    }

    /**
     * Method used for load previous chat history
     */
    private void loadChatHistory() {
        progressBarMessage.setVisibility(View.VISIBLE);
        ChatHelper.getInstance().loadChatHistory(qbDialogForchat, skipPagination, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                // The newest messages should be in the end of list,
                // so we need to reverse list to show messages in the right order
                Log.e(TAG, "Message-Size=====>" + messages.size());
                Collections.reverse(messages);
                if (chatAdapter == null) {
                    chatAdapter = new QbChatMessageAdapter(getActivity(), messages);

                    if (unShownMessages != null && !unShownMessages.isEmpty()) {
                        List<QBChatMessage> chatList = chatAdapter.getList();
                        for (QBChatMessage message : unShownMessages) {
                            if (!chatList.contains(message)) {
                                chatAdapter.addMessage(message);
                            }
                        }
                    }
                    messagesListView.setAdapter(chatAdapter);
                    messagesListView.setAreHeadersSticky(false);
                    messagesListView.setDivider(null);
                    progressBarMessage.setVisibility(View.GONE);
                } else {
                    chatAdapter.addMessageList(messages);
                    progressBarMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                progressBarMessage.setVisibility(View.GONE);
                skipPagination -= ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
                snackbar = ErrorUtils.showSnackbar(mainView, R.string.no_internet_connection, e, R.string.dlg_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        loadChatHistory();
                    }
                });
            }
        });
        skipPagination += ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
    }

    /**
     * Listner used for receving and showing Message from server
     */
    private QBChatMessageListener chatMessageListener = new QBChatMessageListener() {
        @Override
        public void onQBChatMessageReceived(QBChat chat, QBChatMessage message) {
            showMessage(message);
            try {
                chat.readMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Method used for showing incoming messsage
     * @param message
     */
    public void showMessage(final QBChatMessage message) {
        if (chatAdapter != null) {
            QbChatFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatAdapter.addMessage(message);
                    scrollMessageListDown();
                }
            });


        }
    }

    /**
     * Method used for set list scroll on bottom
     */
    private void scrollMessageListDown() {
        messagesListView.setSelection(messagesListView.getCount() - 1);
    }

    /**
     * Method used for send new message to server
     * @param messageText
     */
    private void sendChatMessage(String messageText) {
        QBUser qbUser = ChatHelper.getCurrentUser();
        if (ConnectivityUtils.isNetworkConnected()) {
            long milliseconds = new Date().getTime() + QbApp.getInstance().getTimeDifference();
            QBChatMessage chatMessage = new QBChatMessage();
            chatMessage.setBody(messageText);

            chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
            chatMessage.setDateSent(milliseconds);
            chatMessage.setProperty("time", String.valueOf(milliseconds));
            if (qbUser != null) {
                chatMessage.setProperty("sendername", qbUser.getFullName());
            }
            try {

                chat.sendMessage(chatMessage);
                messageEditText.setText("");
                if (qbDialogForchat.getType() == QBDialogType.PRIVATE) {
                    if (Isonline.equalsIgnoreCase("Offline")) {
                        sendPushMessage(messageText, QbDialogUtils.getOpponentIdForPrivateDialog(qbDialogForchat));
                    }
                }

            } catch (XMPPException | SmackException e) {
                Log.e(TAG, "Failed to send a message", e);
                QbChatFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toaster.shortToast(R.string.chat_send_message_error);
                    }
                });

            }
        } else {
            Toaster.shortToast(getResources().getString(R.string.connection_internet_error));
        }
    }

    /**
     * Method used for check  opponent User is online or offline
     * @param opponentID
     */
    public void isUserAvailable(int opponentID) {
        QBPresence presence = ChatHelper.getInstance().getChatRoster().getPresence(opponentID);
        if (presence == null) {
            Log.e("QBPresence", "User not present");
            // No user in your roster
            return;
        }
        actionbarchange(presence);
    }

    /**
     * Method used for change actionbar title for user online or offline
     * @param presence
     */
    private void actionbarchange(final QBPresence presence) {
        Log.e("PresenceChange", "Yes");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String nameLatter = QbDialogUtils.getDialogName(qbDialogForchat);
                nameLatter = nameLatter.substring(0, 1).toUpperCase() + nameLatter.substring(1);
                if (presence.getType() == QBPresence.Type.online) {
                    Isonline = "Online";
                    ((QbActivity) getActivity()).setToolBarTitle(nameLatter + "\n" + "Online");
                } else {
                    Isonline = "Offline";
                    ((QbActivity) getActivity()).setToolBarTitle(nameLatter + "\n" + "Offline");
                }
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        Log.e("onTextChanged", "Yes");
        if (count == 0) {
            chat.stopIstyping();
            Log.e("stopIstyping", "Yes");
        } else {
            chat.sendIstyping();
            Log.e("sendIstyping", "Yes");
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * Method used for send pushmessage to offline user
     * @param Message
     * @param userId
     */
    private void sendPushMessage(final String Message, final int userId) {
        StringifyArrayList<Integer> listuserIds = new StringifyArrayList<Integer>();
        listuserIds.add(userId);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", Message);
            jsonObject.put("broadcastcreatedId", QbApp.getInstance().getSharedPrefsHelper().getQbUser().getId().toString().trim());
            jsonObject.put("broadcastcreatedName", QbApp.getInstance().getSharedPrefsHelper().getQbUser().getFullName());
            jsonObject.put("messageType", QbApp.getInstance().getSharedPrefsHelper().getQbUser().getFullName()+" sent message");
        } catch (Exception e) {
            Log.e("ErrorJson", e.getMessage());
        }

        QbBroadcastSend qbBroadcastSend = new QbBroadcastSend(new QbMyCallBack() {
            @Override
            public void qbSuccess() {
                Log.e("Push", "Success");
                //Toaster.shortToast("Broadcast sent successfully");
            }

            @Override
            public void qbFailure() {
                // sendPushMessage(Message,userId);
                Log.e("Push", "Fail");
                //Toaster.shortToast("Please try after some time.");
            }
        });
        qbBroadcastSend.sendPush(listuserIds, jsonObject.toString());
    }

}
