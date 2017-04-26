package com.indianic.qbchat.utils.chat;

import android.os.Bundle;
import android.util.Log;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.R;
import com.indianic.qbchat.utils.Toaster;
import com.indianic.qbchat.utils.qb.QbDialogUtils;
import com.indianic.qbchat.utils.qb.QbUsersHolder;
import com.indianic.qbchat.utils.qb.callback.QbEntityCallbackTwoTypeWrapper;
import com.indianic.qbchat.utils.qb.callback.QbEntityCallbackWrapper;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.LogLevel;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ChatHelper {
    private static final String TAG = ChatHelper.class.getSimpleName();

    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    public static final int DIALOG_ITEMS_PER_PAGE = 100;
    public static final int CHAT_HISTORY_ITEMS_PER_PAGE = 50;
    private static final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";

    private static ChatHelper instance;

    private QBChatService qbChatService;
    private QBRoster chatRoster;
    QBRosterListener rosterListener;

    public static synchronized ChatHelper getInstance() {
        if (instance == null) {
            QBSettings.getInstance().setLogLevel(LogLevel.DEBUG);
            QBChatService.setDebugEnabled(true);
            QBChatService.setDefaultPacketReplyTimeout(10000);//set reply timeout in milliseconds for connection's packet.
            QBChatService.setDefaultAutoSendPresenceInterval(AUTO_PRESENCE_INTERVAL_IN_SECONDS);
            QBChatService.ConfigurationBuilder chatServiceConfigurationBuilder = new QBChatService.ConfigurationBuilder();
            chatServiceConfigurationBuilder.setSocketTimeout(60); //Sets chat socket's read timeout in seconds
            chatServiceConfigurationBuilder.setKeepAlive(true); //Sets connection socket's keepAlive option.
            chatServiceConfigurationBuilder.setUseTls(true); //Sets the TLS security mode used when making the connection. By default TLS is disabled.
            QBChatService.setConfigurationBuilder(chatServiceConfigurationBuilder);
            instance = new ChatHelper();
        }
        return instance;
    }

    public static QBUser getCurrentUser() {
        return QBChatService.getInstance().getUser();
    }

    private ChatHelper() {
        qbChatService = QBChatService.getInstance();
        qbChatService.setUseStreamManagement(true);

    }

    public QBRoster getChatRoster() {
        return chatRoster;
    }

    public QBRosterListener getRosterListener() {
        return rosterListener;
    }

    public QBChatService getQbChatService() {
        return qbChatService;
    }

    public void addConnectionListener(ConnectionListener listener) {
        qbChatService.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        qbChatService.removeConnectionListener(listener);
    }

    public void newSessionCreate(QBEntityCallback<QBSession> callback) {
        QBAuth.createSession(callback);
    }

    public void oldSessionset(String oldToken, Date expiredDate) {
        try {
            QBAuth.createFromExistentToken(oldToken, expiredDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loginUser(final QBUser qbUser, QBEntityCallback<QBUser> callback) {
        QBUsers.signIn(qbUser, callback);
    }

    public void signUpUser(final QBUser qbUser, QBEntityCallback<QBUser> callback) {
        QBUsers.signUpSignInTask(qbUser, callback);
    }

    public void loginForChatwithNewToken(final QBUser user, final QBEntityCallback<Void> callback) {
        // Create REST API session on QuickBlox
        QBAuth.createSession(user, new QbEntityCallbackTwoTypeWrapper<QBSession, Void>(callback) {
            @Override
            public void onSuccess(QBSession session, Bundle args) {
                user.setId(session.getUserId());
                loginToChat(user, new QbEntityCallbackWrapper<>(callback));
            }
        });
    }

    public void loginForChatwithOldToken(final QBUser user, String oldToken, Date expiredDate, final QBEntityCallback<Void> callback) {
        try {
            QBAuth.createFromExistentToken(oldToken, expiredDate);
            loginToChat(user, new QbEntityCallbackWrapper<>(callback));
        } catch (Exception e) {
        }

    }

    public void loginToChat(final QBUser user, final QBEntityCallback<Void> callback) {
        if (qbChatService.isLoggedIn()) {
            callback.onSuccess(null, null);
            return;
        }

        qbChatService.login(user, new QbEntityCallbackWrapper<Void>(callback) {
            @Override
            public void onSuccess(Void o, Bundle bundle) {
                super.onSuccess(o, bundle);
                roasterList();
            }

            @Override
            public void onError(QBResponseException e) {
                super.onError(e);
            }
        });
    }

    public boolean logout() {
        try {
            qbChatService.logout();
            return true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void retrieveAllUsersFromPage(QBPagedRequestBuilder qbPagedRequestBuilder, QBEntityCallback<ArrayList<QBUser>> callback) {
        QBUsers.getUsers(qbPagedRequestBuilder, callback);
    }

    public void retrieveAllUsersFromId(ArrayList<Integer> usersIds, QBPagedRequestBuilder qbPagedRequestBuilder, QBEntityCallback<ArrayList<QBUser>> callback) {
        QBUsers.getUsersByIDs(usersIds, qbPagedRequestBuilder, callback);
    }

    public void createDialogWithSelectedUsers(final List<QBUser> users, final int isAction, final String groupName,
                                              final QBEntityCallback<QBDialog> callback) {
        QBChatService.getInstance().getGroupChatManager().createDialog(QbDialogUtils.createDialog(users, isAction, groupName),
                new QbEntityCallbackWrapper<QBDialog>(callback) {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle args) {
                        QbUsersHolder.getInstance().putUsers(users);
                        super.onSuccess(dialog, args);
                    }
                }
        );
    }

    public void deleteDialogs(Collection<QBDialog> dialogs, boolean forcedelete, QBEntityCallback<Void> callback) {
        for (QBDialog dialog : dialogs) {
            deleteDialog(dialog, forcedelete, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                }

                @Override
                public void onError(QBResponseException e) {
                }
            });
        }

        callback.onSuccess(null, null);
    }

    public void deleteDialog(QBDialog qbDialog, boolean forcedelete, QBEntityCallback<Void> callback) {
        if (qbDialog.getType() == QBDialogType.GROUP) {
            QBChatService.getInstance().getGroupChatManager().deleteDialog(qbDialog.getDialogId(), forcedelete,
                    new QbEntityCallbackWrapper<>(callback));
        } else if (qbDialog.getType() == QBDialogType.PRIVATE) {
            QBChatService.getInstance().getPrivateChatManager().deleteDialog(qbDialog.getDialogId(), forcedelete,
                    new QbEntityCallbackWrapper<>(callback));
        } else if (qbDialog.getType() == QBDialogType.PUBLIC_GROUP) {
            Toaster.shortToast(R.string.public_group_chat_cannot_be_deleted);
        }
    }

    public void leaveDialog(QBDialog qbDialog, QBEntityCallback<QBDialog> callback) {
        QBRequestUpdateBuilder qbRequestBuilder = new QBRequestUpdateBuilder();
        qbRequestBuilder.pullAll("occupants_ids", QbApp.getInstance().getSharedPrefsHelper().getQbUser().getId());

        QBChatService.getInstance().getGroupChatManager().updateDialog(qbDialog, qbRequestBuilder,
                new QbEntityCallbackWrapper<QBDialog>(callback) {
                    @Override
                    public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                        super.onSuccess(qbDialog, bundle);
                    }
                });
    }

    public void updateDialogUsers(QBDialog qbDialog,
                                  final List<QBUser> newQbDialogUsersList,
                                  QBEntityCallback<QBDialog> callback) {
        List<QBUser> addedUsers = QbDialogUtils.getAddedUsers(qbDialog, newQbDialogUsersList);
        List<QBUser> removedUsers = QbDialogUtils.getRemovedUsers(qbDialog, newQbDialogUsersList);

        QbDialogUtils.logDialogUsers(qbDialog);
        QbDialogUtils.logUsers(addedUsers);
        Log.w(TAG, "=======================");
        QbDialogUtils.logUsers(removedUsers);

        QBRequestUpdateBuilder qbRequestBuilder = new QBRequestUpdateBuilder();
        if (!addedUsers.isEmpty()) {
            qbRequestBuilder.pushAll("occupants_ids", QbDialogUtils.getUserIds(addedUsers));
        }
        if (!removedUsers.isEmpty()) {
            qbRequestBuilder.pullAll("occupants_ids", QbDialogUtils.getUserIds(removedUsers));
        }
        qbDialog.setName(QbDialogUtils.createChatNameFromUserList(newQbDialogUsersList));

        QBChatService.getInstance().getGroupChatManager().updateDialog(qbDialog, qbRequestBuilder,
                new QbEntityCallbackWrapper<QBDialog>(callback) {
                    @Override
                    public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                        QbUsersHolder.getInstance().putUsers(newQbDialogUsersList);
                        QbDialogUtils.logDialogUsers(qbDialog);
                        super.onSuccess(qbDialog, bundle);
                    }
                });
    }

    public void loadChatHistory(QBDialog dialog, int skipPagination,
                                final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setSkip(skipPagination);
        customObjectRequestBuilder.setLimit(CHAT_HISTORY_ITEMS_PER_PAGE);
        customObjectRequestBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD);

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder,
                new QbEntityCallbackWrapper<ArrayList<QBChatMessage>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                        getUsersFromMessages(qbChatMessages, callback);
                        // Not calling super.onSuccess() because
                        // we're want to load chat users before triggering the callback
                    }
                });
    }

    public void getDialogs(QBRequestGetBuilder customObjectRequestBuilder, final QBEntityCallback<ArrayList<QBDialog>> callback) {
        customObjectRequestBuilder.setLimit(DIALOG_ITEMS_PER_PAGE);

        QBChatService.getChatDialogs(null, customObjectRequestBuilder,
                new QbEntityCallbackWrapper<ArrayList<QBDialog>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBDialog> dialogs, Bundle args) {
                        Iterator<QBDialog> dialogIterator = dialogs.iterator();
                        while (dialogIterator.hasNext()) {
                            QBDialog dialog = dialogIterator.next();
                            if (dialog.getType() == QBDialogType.PUBLIC_GROUP) {
                                dialogIterator.remove();
                            }
                        }

                        for (QBDialog dialog : dialogs) {
                            dialog.setId(dialog.getDialogId().hashCode());
                        }
                        getUsersFromDialogs(dialogs, callback);
                        // Not calling super.onSuccess() because
                        // we want to load chat users before triggering callback
                    }
                });
    }

    public void getUsersFromDialog(QBDialog dialog,
                                   final QBEntityCallback<ArrayList<QBUser>> callback) {
        List<Integer> userIds = dialog.getOccupants();

        ArrayList<QBUser> users = new ArrayList<>(userIds.size());
        for (Integer id : userIds) {
            users.add(QbUsersHolder.getInstance().getUserById(id));
        }

        // If we already have all users in memory
        // there is no need to make REST requests to QB
        if (userIds.size() == users.size()) {
            callback.onSuccess(users, null);
            return;
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder,
                new QbEntityCallbackWrapper<ArrayList<QBUser>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                        QbUsersHolder.getInstance().putUsers(qbUsers);
                        super.onSuccess(qbUsers, bundle);
                    }
                });
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback) {
        loadFileAsAttachment(file, callback, null);
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback,
                                     QBProgressCallback progressCallback) {
        QBContent.uploadFileTask(file, true, null,
                new QbEntityCallbackTwoTypeWrapper<QBFile, QBAttachment>(callback) {
                    @Override
                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
                        attachment.setId(qbFile.getId().toString());
                        attachment.setUrl(qbFile.getPublicUrl());
                        onSuccessInMainThread(attachment, bundle);
                    }
                }, progressCallback);
    }

    private void getUsersFromDialogs(final ArrayList<QBDialog> dialogs,
                                     final QBEntityCallback<ArrayList<QBDialog>> callback) {
        List<Integer> userIds = new ArrayList<>();
        for (QBDialog dialog : dialogs) {
            userIds.addAll(dialog.getOccupants());
            userIds.add(dialog.getLastMessageUserId());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder,
                new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBDialog>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        QbUsersHolder.getInstance().putUsers(users);
                        onSuccessInMainThread(dialogs, params);
                    }
                });
    }

    private void getUsersFromMessages(final ArrayList<QBChatMessage> messages,
                                      final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        Set<Integer> userIds = new HashSet<>();
        for (QBChatMessage message : messages) {
            userIds.add(message.getSenderId());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder,
                new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBChatMessage>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        QbUsersHolder.getInstance().putUsers(users);
                        onSuccessInMainThread(messages, params);
                    }
                });
    }


    public void roasterList() {
        rosterListener = new QBRosterListener() {
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
                Log.e("Roaster", "Presence change roaster" + presence.getUserId() + "==" + presence.getType());
                //isUserAvailable(presence.getUserId());


            }
        };


        QBSubscriptionListener subscriptionListener = new QBSubscriptionListener() {
            @Override
            public void subscriptionRequested(int userId) {
                addtoRoaster(userId);
                Log.e("RoasterChatService", "subbbbbbbb roaster=====" + userId);
            }
        };


        // Do this after success Chat login
        chatRoster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual, subscriptionListener);
        chatRoster.addRosterListener(rosterListener);

    }

    public void addtoRoaster(int userID) {
        if (chatRoster.contains(userID)) {
            try {
                chatRoster.subscribe(userID);
                chatRoster.confirmSubscription(userID);
                Log.e("RoasterChatService", "Subscription roaster");
            } catch (SmackException.NotConnectedException e) {
                Log.e("Suberror", e.getMessage());
            } catch (XMPPException | SmackException.NotLoggedInException | SmackException.NoResponseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                chatRoster.createEntry(userID, null);
                chatRoster.confirmSubscription(userID);
                Log.e("Roaster", "Addedto roaster" + userID);
            } catch (XMPPException | SmackException.NotLoggedInException | SmackException.NoResponseException | SmackException.NotConnectedException e) {
                Log.e("E", e.getMessage());
            }
        }
    }


}