package com.indianic.qbchat.fragment;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.R;
import com.indianic.qbchat.activity.QbActivity;
import com.indianic.qbchat.adapter.QbUsersAdapter;
import com.indianic.qbchat.commanclass.QbBroadcastSend;
import com.indianic.qbchat.commanclass.QbCreateDialog;
import com.indianic.qbchat.commanclass.QbRetriveAllUser;
import com.indianic.qbchat.utils.ConnectivityUtils;
import com.indianic.qbchat.utils.ErrorUtils;
import com.indianic.qbchat.utils.QbConstant;
import com.indianic.qbchat.utils.Toaster;
import com.indianic.qbchat.utils.qb.callback.QbDialogCreateCallBack;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;
import com.indianic.qbchat.utils.qb.callback.QbRetriveUserCallback;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Created by PG on 24/05/16.
 */
public class QbUserListFragment extends QbBaseFragment {

    public static final int MINIMUM_CHAT_OCCUPANTS_SIZE = 1;
    private ListView usersListView;
    private ProgressBar progressBar;
    private QbUsersAdapter usersAdapter;
    Snackbar snackbar;
    View viewsnackbar;
    private Button btn_startchat, btn_audiocall, btn_videocall;
    private int isAction = 0;
    private LinearLayout linear_call_button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.qbfragment_select_users, container, false);
    }

    @Override
    public void initView(View view) {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            isAction = bundle.getInt(QbConstant.IsActionBroadcast);
        }
        linear_call_button = _findViewById(R.id.linear_call_botton, view);
        progressBar = _findViewById(R.id.progress_select_users, view);
        usersListView = _findViewById(R.id.list_select_users, view);
        btn_startchat = _findViewById(R.id.btn_startchat, view);
        btn_audiocall = _findViewById(R.id.btn_audiocall, view);
        btn_videocall = _findViewById(R.id.btn_videocall, view);
        btn_audiocall.setOnClickListener(this);
        btn_videocall.setOnClickListener(this);
        btn_startchat.setOnClickListener(this);
        viewsnackbar = _findViewById(R.id.layout_root, view);
        if (ConnectivityUtils.isNetworkConnected()) {
            loadUsersFromQb();
        } else {
            snackbar = ErrorUtils.showSnackbar(view, R.string.no_internet_connection, R.string.dlg_retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    loadUsersFromQb();
                }
            });
        }
        if (isAction == QbConstant.requestForBroadcast) {
            btn_startchat.setText(getString(R.string.string_sendbroadcast));
        } else if (isAction == QbConstant.requestForGroupcreate) {
            btn_startchat.setText(getString(R.string.string_create_group));
        } else if (isAction == QbConstant.requestForCall) {
            linear_call_button.setVisibility(View.VISIBLE);
            btn_startchat.setVisibility(View.INVISIBLE);
        }
        ((QbActivity) getActivity()).setToolBarTitle(getString(R.string.string_userlist));
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.btn_startchat) {
            if (isAction == QbConstant.requestSinglechat) {
                if (usersAdapter != null) {
                    List<QBUser> users = usersAdapter.getSelectedUsers();
                    if (users.size() >= MINIMUM_CHAT_OCCUPANTS_SIZE) {
                        createDialog(usersAdapter.getSelectedUsers(), "");
                    } else {
                        Toaster.shortToast(R.string.select_users_choose_users);
                    }
                }
            } else if (isAction == QbConstant.requestForBroadcast) {
                List<QBUser> users = usersAdapter.getSelectedUsers();
                if (users.size() >= MINIMUM_CHAT_OCCUPANTS_SIZE) {
                    alertBroadcastCreate();
                } else {
                    Toaster.shortToast(getString(R.string.string_broadcast_error));

                }
            } else if (isAction == QbConstant.requestForGroupcreate) {
                List<QBUser> users = usersAdapter.getSelectedUsers();
                if (users.size() >= MINIMUM_CHAT_OCCUPANTS_SIZE) {
                    alertGroupCreate(usersAdapter.getSelectedUsers());
                } else {
                    Toaster.shortToast(getString(R.string.string_groupcreate_error));
                }
            }

        } else {
            if (usersAdapter != null) {
                List<QBUser> users = usersAdapter.getSelectedUsers();
                if (users.size() >= MINIMUM_CHAT_OCCUPANTS_SIZE) {
                    ((QbActivity) getActivity()).getUserList().clear();
                    ((QbActivity) getActivity()).getUserList().addAll(usersAdapter.getSelectedUsers());
                    QBRTCTypes.QBConferenceType qbConferenceType = null;

                    //Init conference type

                    if (i == R.id.btn_audiocall) {
                        qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

                    } else if (i == R.id.btn_videocall) {// get call type
                        qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
                    }
                    Map<String, String> userInfo = new HashMap<>();
                    String oncallUserNames="",oncallUserId="";
                    if(users.size()>1) {
                        userInfo.put(QbConstant.ONCALLETOTALUSER,String.valueOf(users.size()));
                        for (int p = 0; p < users.size(); p++) {
                            if (oncallUserNames.length() > 0) {
                                oncallUserNames = oncallUserNames + "," + users.get(p).getFullName();
                            } else {
                                oncallUserNames = users.get(p).getFullName();
                            }

                            if (oncallUserId.length() > 0) {
                                oncallUserId = oncallUserId + "," + users.get(p).getId();
                            } else {
                                oncallUserId = String.valueOf(users.get(p).getId());
                            }

                        }
                    }
                    else
                    {
                        userInfo.put(QbConstant.ONCALLETOTALUSER,"1");
                        oncallUserNames=QbApp.getInstance().getSharedPrefsHelper().getQbUser().getFullName();
                        oncallUserId=String.valueOf(QbApp.getInstance().getSharedPrefsHelper().getQbUser().getId());
                    }
                    userInfo.put(QbConstant.CALLERQUICKBLOXID,String.valueOf(QbApp.getInstance().getSharedPrefsHelper().getQbUser().getId()));
                    userInfo.put(QbConstant.CALLERQUICKBLOXNAME,QbApp.getInstance().getSharedPrefsHelper().getQbUser().getFullName());
                    userInfo.put(QbConstant.USERONCALL, oncallUserNames);
                    userInfo.put(QbConstant.USERONCALLQUICKBLOXID, oncallUserId);

                    ((QbActivity) getActivity())
                            .addConversationFragmentStartCall(usersAdapter.getSelectedUsers(),
                                    qbConferenceType, userInfo);
                } else {
                    Toaster.shortToast(R.string.select_users_choose_users);
                }
            } else {
                Toaster.shortToast(R.string.select_users_choose_users);
            }

        }
    }

    /**
     * Method used for geting list of registed user from quickblox server
     */
    private void loadUsersFromQb() {
        progressBar.setVisibility(View.VISIBLE);
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(100);
        QbRetriveAllUser qbRetriveAllUser = new QbRetriveAllUser(new QbRetriveUserCallback() {
            @Override
            public void issuccess(ArrayList<QBUser> qbUsersList) {
                QBUser qbUser = QbApp.getInstance().getSharedPrefsHelper().getQbUser();
                for (int i = 0; i < qbUsersList.size(); i++) {
                    if (qbUsersList.get(i).getId().toString().equalsIgnoreCase(qbUser.getId().toString())) {
                        Log.e("Remove", "Yes" + qbUsersList.get(i));
                        qbUsersList.remove(i);
                    }
                }
                usersAdapter = new QbUsersAdapter(getActivity(), qbUsersList, isAction);
                usersListView.setAdapter(usersAdapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void isfail(Exception e) {
                snackbar = ErrorUtils.showSnackbar(viewsnackbar, R.string.select_users_get_users_error, e, R.string.dlg_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        loadUsersFromQb();
                    }
                });
                progressBar.setVisibility(View.GONE);
            }
        });
        qbRetriveAllUser.callForUser(pagedRequestBuilder);
    }

    /**
     * Method used for creating dialog for chat
     *
     * @param qbUserList
     * @param groupName
     */
    private void createDialog(final List<QBUser> qbUserList, final String groupName) {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        QbCreateDialog qbCreateDialog = new QbCreateDialog(new QbDialogCreateCallBack() {
            @Override
            public void issuccess(QBDialog qbDialog) {
                if (progressDialog.isShowing()) {
                    progressDialog.cancel();
                }
                usersAdapter.clearSelected();
                Bundle bundles = new Bundle();
                bundles.putSerializable(QbChatFragment.EXTRA_DIALOG, qbDialog);
                QbChatFragment chatFragment = new QbChatFragment();
                chatFragment.setArguments(bundles);
                ((QbActivity) getActivity()).addFragment(chatFragment, QbUserListFragment.this);


            }

            @Override
            public void isfail(Exception e) {
                if (progressDialog.isShowing()) {
                    progressDialog.cancel();
                }
                snackbar = ErrorUtils.showSnackbar(viewsnackbar, R.string.connection_chat_error, e, R.string.dlg_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        createDialog(qbUserList, groupName);
                    }
                });
            }
        });
        qbCreateDialog.createDialog(qbUserList, isAction, groupName);

    }

    /**
     * Method used for show alert dialog for creating broadcast
     */
    private void alertBroadcastCreate() {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.qb_broadcast_dialog, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.edit_broad_message);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!TextUtils.isEmpty(userInput.getText().toString().trim())) {
                                    sendPushBroadcast(userInput.getText().toString().trim());
                                } else {
                                    Toaster.shortToast("Please enter broadcast message.");
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        // show it
        alertDialog.show();
    }

    /**
     * Method used for show alert dialog for creating Group
     */
    private void alertGroupCreate(final List<QBUser> qbUserList) {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.qb_broadcast_dialog, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.edit_broad_message);
        final TextView ttvGroupname = (TextView) promptsView.findViewById(R.id.qb_chat_tv_title);
        ttvGroupname.setText(getString(R.string.string_groupname));
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!TextUtils.isEmpty(userInput.getText().toString().trim())) {
                                    createDialog(qbUserList, userInput.getText().toString().trim());
                                } else {
                                    Toaster.shortToast(getString(R.string.string_groupnameError));
                                }
                            }
                        })
                .setNegativeButton(getString(R.string.string_cancle),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }


    @Override
    public void initActionBar() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ((QbActivity) getActivity()).setToolBarTitle(getString(R.string.string_userlist));
    }

    /**
     * Method used for send broadcast push notification
     */
    private void sendPushBroadcast(final String broadcastMessage) {
        progressDialog.show();
        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
        for (int i = 0; i < usersAdapter.getSelectedUsers().size(); i++) {
            userIds.add(usersAdapter.getSelectedUsers().get(i).getId());
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(QbConstant.MESSAGES, broadcastMessage);
            jsonObject.put(QbConstant.BROADCASTID, QbApp.getInstance().getSharedPrefsHelper().getQbUser().getId().toString().trim());
            jsonObject.put(QbConstant.BROADCASTCREATEDNAME, QbApp.getInstance().getSharedPrefsHelper().getQbUser().getFullName());
            jsonObject.put(QbConstant.MESSAGETYPE, getString(R.string.string_broadcastfrom) + " " + QbApp.getInstance().getSharedPrefsHelper().getQbUser().getFullName());
        } catch (Exception e) {
            Log.e("ErrorJson", e.getMessage());
        }

        QbBroadcastSend qbBroadcastSend = new QbBroadcastSend(new QbMyCallBack() {
            @Override
            public void qbSuccess() {
                progressDialog.cancel();
                usersAdapter.clearSelected();
                Toaster.shortToast(getString(R.string.string_broadcast_create_successfully));
            }

            @Override
            public void qbFailure() {
                progressDialog.cancel();
                Toaster.shortToast(getString(R.string.string_try_sometime));
            }
        });
        qbBroadcastSend.sendPush(userIds, jsonObject.toString());
    }

}
