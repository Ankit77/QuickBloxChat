package com.indianic.qbchat.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.R;
import com.indianic.qbchat.activity.QbActivity;
import com.indianic.qbchat.adapter.QbOldMessagesAdapter;
import com.indianic.qbchat.commanclass.QbSubscribePush;
import com.indianic.qbchat.utils.ConnectivityUtils;
import com.indianic.qbchat.utils.ErrorUtils;
import com.indianic.qbchat.utils.QbConstant;
import com.indianic.qbchat.utils.Toaster;
import com.indianic.qbchat.utils.chat.ChatHelper;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;

import java.util.ArrayList;

/**
 * Created by PG on 24/05/16.
 */
public class QbMessagesFragment extends QbBaseFragment {
    private final String TAG = QbMessagesFragment.class.getSimpleName();
    private View viewsnakbar;
    Snackbar snackbar = null;
    private int skipRecords = 0;
    private ListView listView_previouseChat;
    private QbOldMessagesAdapter dialogsAdapter;
    AlertDialog.Builder alertDialogDelete;
    private TextView tv_nomessages;
    private FloatingActionButton floatingActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.qbfragment_old_chat_history, container, false);
    }

    @Override
    public void initView(View view) {
        setHasOptionsMenu(true);
        boolean isRegistedToPush = QbApp.getInstance().getSharedPrefsHelper().getPrefValue(QbConstant.Pref_IsRegistedToQuickbloxforPush, false);
        if (!isRegistedToPush) {
            sendRegistrationToServer(QbApp.getInstance().getSharedPrefsHelper().getPrefValue(QbConstant.Pref_GCMToken, ""));
        }
        floatingActionButton = _findViewById(R.id.fab_dialogs_new_chat, view);
        tv_nomessages = _findViewById(R.id.tv_nomessagefound, view);
        viewsnakbar = _findViewById(R.id.activity_previous_chat_linearmain, view);
        listView_previouseChat = _findViewById(R.id.list_dialogs_chats, view);
        listView_previouseChat.setDivider(null);


        if (ConnectivityUtils.isNetworkConnected()) {
            getPreviousChat();
        } else {
            snackbar = ErrorUtils.showSnackbar(viewsnakbar, R.string.no_internet_connection, R.string.dlg_retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    getPreviousChat();
                }
            });
        }
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((QbActivity) getActivity()).addFragment(new QbUserListFragment(), QbMessagesFragment.this);
            }
        });
        ((QbActivity) getActivity()).setToolBarTitle(getString(R.string.messages));
    }

    /**
     * Methos used for alert to delete old message history
     *
     * @param qbDialog
     */
    public void deleteAlert(final QBDialog qbDialog) {
        alertDialogDelete = new AlertDialog.Builder(getActivity());
        alertDialogDelete.setMessage(getString(R.string.string_alertmessage_delete));
        alertDialogDelete.setPositiveButton(getString(R.string.string_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteChat(qbDialog, dialog);
            }
        });
        alertDialogDelete.setNegativeButton(getString(R.string.string_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogDelete.show();
    }

    /**
     * Methos used to delete old message history
     *
     * @param qbDialog
     * @param dialog
     */
    public void deleteChat(final QBDialog qbDialog, final DialogInterface dialog) {
        progressDialog.show();
        if (ConnectivityUtils.isNetworkConnected()) {
            ChatHelper.getInstance().deleteDialog(qbDialog, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    progressDialog.cancel();
                    dialogsAdapter.remove(qbDialog);
                    dialogsAdapter.notifyDataSetChanged();
                    if (dialogsAdapter.getCount() == 0) {
                        tv_nomessages.setVisibility(View.VISIBLE);
                    } else {
                        tv_nomessages.setVisibility(View.GONE);
                    }
                    dialog.cancel();
                    Toaster.shortToast(getString(R.string.string_chatdeleted_success));
                }

                @Override
                public void onError(QBResponseException e) {
                    progressDialog.cancel();
                    Toaster.shortToast(e.getMessage());
                }
            });
        } else {
            snackbar = ErrorUtils.showSnackbar(viewsnakbar, R.string.no_internet_connection, R.string.dlg_retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    deleteChat(qbDialog, dialog);
                }
            });
        }
    }

    /**
     * Method used for retrive list of all old chat history
     */
    private void getPreviousChat() {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        QBRequestGetBuilder qbRequestBuilder = new QBRequestGetBuilder();
        qbRequestBuilder.setLimit(1000);
        qbRequestBuilder.sortAsc("last_message_date_sent");
        qbRequestBuilder.setSkip(skipRecords = 0);
        ChatHelper.getInstance().getDialogs(qbRequestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> qbDialogs, Bundle bundle) {
                if (progressDialog.isShowing()) {
                    progressDialog.cancel();
                }
                if (qbDialogs.size() == 0) {
                    tv_nomessages.setVisibility(View.VISIBLE);
                } else {
                    tv_nomessages.setVisibility(View.GONE);
                }

                dialogsAdapter = new QbOldMessagesAdapter(QbMessagesFragment.this, qbDialogs);
                listView_previouseChat.setAdapter(dialogsAdapter);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, e.getMessage());
                if (progressDialog.isShowing()) {
                    progressDialog.cancel();
                }
                snackbar = ErrorUtils.showSnackbar(viewsnakbar, R.string.connection_chat_error, e, R.string.dlg_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        getPreviousChat();
                    }
                });
            }
        });
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((QbActivity) getActivity()).setToolBarTitle(getString(R.string.messages));
            if (ConnectivityUtils.isNetworkConnected()) {
                getPreviousChat();
            } else {
                snackbar = ErrorUtils.showSnackbar(viewsnakbar, R.string.no_internet_connection, R.string.dlg_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        getPreviousChat();
                    }
                });
            }
        }
    }

    @Override
    public void initActionBar() {

    }

    /**
     * Method used for user GCM id registed to server for push notification
     *
     * @param registrationID
     */
    private void sendRegistrationToServer(final String registrationID) {
        // Add custom implementation, as needed.
        QbSubscribePush qbSubscribePush = new QbSubscribePush(new QbMyCallBack() {
            @Override
            public void qbSuccess() {
                QbApp.getInstance().getSharedPrefsHelper().saveToPrefrance(QbConstant.Pref_IsRegistedToQuickbloxforPush, true);

            }

            @Override
            public void qbFailure() {
                sendRegistrationToServer(registrationID);
            }
        }, getActivity());
        qbSubscribePush.subscribeToPushNotifications(registrationID);


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainmenu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == R.id.new_group) {
            Bundle bundle = new Bundle();
            bundle.putInt(QbConstant.IsActionBroadcast, QbConstant.requestForGroupcreate);
            QbUserListFragment qbUserListFragment = new QbUserListFragment();
            qbUserListFragment.setArguments(bundle);
            ((QbActivity) getActivity()).addFragment(qbUserListFragment, this);
        } else if (i == R.id.new_broadcast) {
            Bundle bundle = new Bundle();
            bundle.putInt(QbConstant.IsActionBroadcast, QbConstant.requestForBroadcast);
            QbUserListFragment qbUserListFragment = new QbUserListFragment();
            qbUserListFragment.setArguments(bundle);
            ((QbActivity) getActivity()).addFragment(qbUserListFragment, this);
        } else if (i == R.id.new_call) {
            Bundle bundle = new Bundle();
            bundle.putInt(QbConstant.IsActionBroadcast, QbConstant.requestForCall);
            QbUserListFragment qbUserListFragment = new QbUserListFragment();
            qbUserListFragment.setArguments(bundle);
            ((QbActivity) getActivity()).addFragment(qbUserListFragment, this);
        } else if (i == R.id.signout) {
            try {
                ChatHelper.getInstance().logout();
            } catch (Exception e) {
            }
            QbApp.getInstance().getSharedPrefsHelper().delete(QbConstant.Pref_UserEmailAddress);
            QbApp.getInstance().getSharedPrefsHelper().delete(QbConstant.Pref_UserQuickBloxId);
            QbApp.getInstance().getSharedPrefsHelper().delete(QbConstant.Pref_UserPassword);
            QbApp.getInstance().getSharedPrefsHelper().delete(QbConstant.Pref_UserFullName);
            QbApp.getInstance().getSharedPrefsHelper().delete(QbConstant.Pref_IsRegistedToQuickbloxforPush);
            Intent mIntent = new Intent(getActivity(), QbActivity.class);
            mIntent.putExtra(QbActivity.isActionType, QbConstant.isActionSignIn);
            getActivity().finish();
            startActivity(mIntent);

        }
        return super.onOptionsItemSelected(item);
    }


}
