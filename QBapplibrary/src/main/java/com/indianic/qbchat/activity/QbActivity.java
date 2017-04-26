package com.indianic.qbchat.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.R;
import com.indianic.qbchat.fragment.ConversationFragment;
import com.indianic.qbchat.fragment.IncomeCallFragment;
import com.indianic.qbchat.fragment.QbSigninFragment;
import com.indianic.qbchat.fragment.QbMessagesFragment;
import com.indianic.qbchat.fragment.QbUserListFragment;
import com.indianic.qbchat.utils.FragmentExecuotr;
import com.indianic.qbchat.utils.KeyboardUtils;
import com.indianic.qbchat.utils.QbConstant;
import com.indianic.qbchat.utils.DialogUtils;
import com.indianic.qbchat.utils.ErrorUtils;
import com.indianic.qbchat.utils.RingtonePlayer;
import com.indianic.qbchat.utils.SettingsUtil;
import com.indianic.qbchat.utils.Toaster;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;

import org.webrtc.VideoCapturerAndroid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QbActivity extends AppCompatActivity implements QBRTCClientSessionCallbacks, QBRTCSessionConnectionCallbacks, QBRTCSignalingCallback {

    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = ConversationFragment.class.getSimpleName();
    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";


    private QBRTCSession currentSession;
    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private BroadcastReceiver wifiStateReceiver;
    private boolean isInCommingCall;
    private boolean isInFront;
    private QBRTCClient rtcClient;
    private QBRTCSessionUserCallback sessionUserCallback;
    private boolean wifiEnabled = true;
    private SharedPreferences sharedPref;
    private RingtonePlayer ringtonePlayer;


    private Toolbar toolbar;

    public static final String isActionType = "isactiontype";
    public static final String emailAddress = "emailAddress";
    public static final String password = "password";
    public static final String fullName = "fullName";
    public static final String opponentUserId = "opponentUserId";
    private ArrayList<QBUser> qbUserArrayList;

//    if isActionResult==0 then SignUp user
//    if isActionResult==1 then SignIn user
//    if isActionResult==2 then ChatUser user
//    if isActionResult==3 then Oldchat History

    private int isActionResult = -1;
    private int opponentUserIdResult = 0;

    protected ProgressDialog progressDialog;
    private final String TAG = QbActivity.class.getSimpleName();
    Snackbar snackbar;
    View viewsnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qbactivity_main);
        progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        initUI();
    }


    protected void initUI() {
        qbUserArrayList = new ArrayList<>();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);


        toolbar = (Toolbar) findViewById(R.id.qb_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.qbic_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Bundle bundleResult = getIntent().getExtras();
        if (bundleResult != null) {
            isActionResult = bundleResult.getInt(isActionType);
            if (isActionResult == QbConstant.isActionSignIn) {
                replaceFragment(new QbSigninFragment());
            } else if (isActionResult == QbConstant.isActionChatUser) {
                opponentUserIdResult = bundleResult.getInt(opponentUserId);
                retriveUser(opponentUserIdResult);
            } else if (isActionResult == QbConstant.isActionVieOldMessage) {
                replaceFragment(new QbMessagesFragment());
            }

        } else {
            finish();
        }
        viewsnackbar = findViewById(R.id.linearMain);

        if (QbApp.getInstance().getSharedPrefsHelper().getQbUser() != null) {
            initvideocall();
        }
    }

    public void initvideocall() {
        initQBRTCClient();
        initWiFiManagerListener();
        ringtonePlayer = new RingtonePlayer(this, R.raw.beep);
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);
        // Add signalling manager
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                if (!createdLocally) {
                    rtcClient.addSignaling((QBWebRTCSignaling) qbSignaling);
                }
            }
        });

        rtcClient.setCameraErrorHendler(new VideoCapturerAndroid.CameraErrorHandler() {
            @Override
            public void onCameraError(final String s) {
                QbActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(QbActivity.this, s, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });


        // Configure
        //
        QBRTCConfig.setMaxOpponentsCount(6);
        QBRTCConfig.setDisconnectTime(30);
        QBRTCConfig.setAnswerTimeInterval(30l);
        QBRTCConfig.setDebugEnabled(true);


        // Add activity as callback to RTCClient
        rtcClient.addSessionCallbacksListener(this);
        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        rtcClient.prepareToProcessCalls();
    }

    private void initWiFiManagerListener() {
        wifiStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "WIFI was changed");
                processCurrentWifiState(context);
            }
        };
    }

    private void processCurrentWifiState(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (wifiEnabled != wifi.isWifiEnabled()) {
            wifiEnabled = wifi.isWifiEnabled();
            Toaster.shortToast("Wifi " + (wifiEnabled ? "enabled" : "disabled"));
        }
    }


    /**
     * Method use for change actionbar title
     *
     * @param title
     */
    public void setToolBarTitle(String title) {
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.qb_chat_tv_title);
        toolbarTitle.setText(title);

    }

    /**
     * Method use for Add new fragment on backStack
     *
     * @param newFragment
     * @param hideFragment
     */
    public void addFragment(final Fragment newFragment, final Fragment hideFragment) {
        getFragmentManager()
                .beginTransaction()
                .add(R.id.container, newFragment, newFragment.getClass().getSimpleName())
                .hide(hideFragment)
                .addToBackStack(hideFragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * Method use for replace fragment
     *
     * @param newFragment
     */
    public void replaceFragment(final Fragment newFragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, newFragment, newFragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * Method use for remove fragment from Stack
     */
    public void popbackstackFragment() {
        getFragmentManager().popBackStack();
    }


    @Override
    public void onBackPressed() {
        KeyboardUtils.hideSoftKeyboard(this);
        Fragment fragment = getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if (fragment == null) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                popbackstackFragment();
            } else {
                super.onBackPressed();
            }
        }
    }


    /**
     * Method use for geting list of registed user from quickblox
     *
     * @param userQuickBloxId
     */
    private void retriveUser(final int userQuickBloxId) {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }

        QBUsers.getUser(userQuickBloxId, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle args) {
                if (progressDialog.isShowing()) {
                    progressDialog.cancel();
                }

                ArrayList<QBUser> qbUserList = new ArrayList<QBUser>();
                qbUserList.add(QbApp.getInstance().getSharedPrefsHelper().getQbUser());
                qbUserList.add(user);
                createDialog(qbUserList);
            }

            @Override
            public void onError(QBResponseException errors) {
                if (progressDialog.isShowing()) {
                    progressDialog.cancel();
                }
                snackbar = ErrorUtils.showSnackbar(viewsnackbar, R.string.connection_chat_error, errors, R.string.dlg_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        retriveUser(userQuickBloxId);
                    }
                });
            }
        });

    }

    private void createDialog(final List<QBUser> qbUserList) {
//        if (!progressDialog.isShowing()) {
//            progressDialog.show();
//        }
//        ChatHelper.getInstance().createDialogWithSelectedUsers(qbUserList, new QBEntityCallback<QBDialog>() {
//            @Override
//            public void onSuccess(QBDialog qbnewDialog, Bundle bundle) {
//                if (progressDialog.isShowing()) {
//                    progressDialog.cancel();
//                }
//
//                Bundle bundles = new Bundle();
//                bundles.putSerializable(QbChatFragment.EXTRA_DIALOG, qbnewDialog);
//                QbChatFragment chatFragment = new QbChatFragment();
//                chatFragment.setArguments(bundles);
//                replaceFragment(chatFragment);
//            }
//
//            @Override
//            public void onError(QBResponseException e) {
//                if (progressDialog.isShowing()) {
//                    progressDialog.cancel();
//                }
//                snackbar = ErrorUtils.showSnackbar(viewsnackbar, R.string.connection_chat_error, e, R.string.dlg_retry, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        snackbar.dismiss();
//                        createDialog(qbUserList);
//                    }
//                });
//            }
//        });
    }

    @Override
    public void onReceiveNewSession(final QBRTCSession session) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.e(TAG, "Session " + session.getSessionID() + " are income paras");
                if (session.getUserInfo() != null) {
                    Log.e("session.getUserInfo()", session.getUserInfo().get(QbConstant.USERONCALL));
                } else {
                    Log.e("session.getUserInfo()", "Nuuuuuuuuuuullll");
                }
                String curSession = (getCurrentSession() == null) ? null : getCurrentSession().getSessionID();

                if (getCurrentSession() == null) {
                    Log.d(TAG, "Start new session");
                    initCurrentSession(session);
                    addIncomeCallFragment(session);

                    isInCommingCall = true;
                    initIncommingCallTask();
                } else {
                    Log.e(TAG, "Stop new session. Device now is busy");
                    session.rejectCall(null);
                }

            }
        });
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer userID) {
        if (!qbrtcSession.equals(getCurrentSession())) {
            return;
        }
        if (sessionUserCallback != null) {
            sessionUserCallback.onUserNotAnswer(qbrtcSession, userID);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
            }
        });
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer userID, Map<String, String> userInfo) {
        if (!qbrtcSession.equals(getCurrentSession())) {
            return;
        }
        if (sessionUserCallback != null) {
            sessionUserCallback.onCallRejectByUser(qbrtcSession, userID, userInfo);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
                removeConversationFragment();
                popbackstackFragment();
                Toaster.shortToast("User " + getString(R.string.rejected) + " conversation");
            }
        });
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer userId, Map<String, String> userInfo) {
        if (!qbrtcSession.equals(getCurrentSession())) {
            return;
        }
        if (sessionUserCallback != null) {
            sessionUserCallback.onCallAcceptByUser(qbrtcSession, userId, userInfo);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ringtonePlayer.stop();
            }
        });
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer) {
        if (qbrtcSession.equals(getCurrentSession())) {

            if (sessionUserCallback != null) {
                sessionUserCallback.onReceiveHangUpFromUser(qbrtcSession, integer);
            }

            //  final String participantName = DataHolder.getUserNameByID(userID);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeConversationFragment();
                    popbackstackFragment();
                    Toaster.shortToast("User " + getString(R.string.hungUp) + " conversation");
                }
            });
        }
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onSessionClosed(final QBRTCSession qbrtcSession) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "Session " + qbrtcSession.getSessionID() + " start stop session");
                String curSession = (getCurrentSession() == null) ? null : getCurrentSession().getSessionID();

                if (qbrtcSession.equals(getCurrentSession())) {

                    Fragment currentFragment = getCurrentFragment();
                    if (isInCommingCall) {
                        //stopIncomeCallTimer();
                        if (currentFragment instanceof IncomeCallFragment) {
                            removeIncomeCallFragment();
                        }
                    }

                    Log.d(TAG, "Stop session");
//                    if (!(currentFragment instanceof OpponentsFragment)) {
//                        addOpponentsFragment();
//                    }

                    releaseCurrentSession();

                    //stopTimer();
                    //closeByWifiStateAllow = true;
                }
            }
        });
    }

    @Override
    public void onSessionStartClose(final QBRTCSession qbrtcSession) {
        qbrtcSession.removeSessionnCallbacksListener(QbActivity.this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(ConversationFragment.class.getSimpleName());
                if (fragment != null && qbrtcSession.equals(getCurrentSession())) {
                    fragment.actionButtonsEnabled(false);
                }
            }
        });
    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {

    }

    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {

    }

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer, QBRTCSignalException e) {

    }

    public interface QBRTCSessionUserCallback {
        void onUserNotAnswer(QBRTCSession session, Integer userId);

        void onCallRejectByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo);

        void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo);

        void onReceiveHangUpFromUser(QBRTCSession session, Integer userId);
    }

    public QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public void initCurrentSession(QBRTCSession sesion) {
        this.currentSession = sesion;
        this.currentSession.addSessionCallbacksListener(QbActivity.this);
        this.currentSession.addSignalingCallback(QbActivity.this);
    }

    private void addIncomeCallFragment(QBRTCSession session) {

        Log.d(TAG, "QBRTCSession in addIncomeCallFragment is " + session);
        if (session != null && isInFront) {
            Fragment fragment = new IncomeCallFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("sessionDescription", session.getSessionDescription());
            bundle.putIntegerArrayList("opponents", new ArrayList<>(session.getOpponents()));
            bundle.putInt(QbConstant.CONFERENCE_TYPE, session.getConferenceType().getValue());
            bundle.putSerializable(QbConstant.USERINFO, (Serializable) session.getUserInfo());
            fragment.setArguments(bundle);
            FragmentExecuotr.addIncomingFragment(getFragmentManager(), R.id.container, fragment, IncomeCallFragment.class.getSimpleName());
        } else {
            Log.e(TAG, "SKIP addIncomeCallFragment method");
        }
    }

    public void addConversationFragmentStartCall(List<QBUser> opponents,
                                                 QBRTCTypes.QBConferenceType qbConferenceType,
                                                 Map<String, String> userInfo) {
        QBRTCSession newSessionWithOpponents = rtcClient.createNewSessionWithOpponents(getOpponentsIds(opponents), qbConferenceType);
        SettingsUtil.setSettingsStrategy(opponents, getDefaultSharedPrefs(), this);
        Log.d("Crash", "addConversationFragmentStartCall. Set session " + newSessionWithOpponents);
        initCurrentSession(newSessionWithOpponents);
        ConversationFragment fragment = ConversationFragment.newInstance((ArrayList<QBUser>) opponents, opponents.get(0).getFullName(),
                qbConferenceType, userInfo,
                StartConversetionReason.OUTCOME_CALL_MADE, getCurrentSession().getSessionID());
        FragmentExecuotr.addIncomingFragment(getFragmentManager(), R.id.container, fragment, CONVERSATION_CALL_FRAGMENT);
        ringtonePlayer.play(true);
    }

    public static ArrayList<Integer> getOpponentsIds(List<QBUser> opponents) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (QBUser user : opponents) {
            ids.add(user.getId());
        }
        return ids;
    }

    private void initIncommingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                IncomeCallFragment incomeCallFragment = (IncomeCallFragment) getFragmentManager().findFragmentByTag(IncomeCallFragment.class.getSimpleName());
                if (incomeCallFragment == null) {
                    ConversationFragment conversationFragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
                    if (conversationFragment != null) {
                        disableConversationFragmentButtons();
                        ringtonePlayer.stop();
                        hangUpCurrentSession();
                    }
                } else {
                    rejectCurrentSession();
                }
                Toast.makeText(QbActivity.this, "Call was stopped by timer", Toast.LENGTH_LONG).show();
            }
        };
    }

    public void rejectCurrentSession() {
        if (getCurrentSession() != null) {
            getCurrentSession().rejectCall(new HashMap<String, String>());
        }
    }

    private void disableConversationFragmentButtons() {
        ConversationFragment fragment = (ConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if (fragment != null) {
            fragment.actionButtonsEnabled(false);
        }
    }

    public static enum StartConversetionReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE;
    }

    @Override
    protected void onResume() {
        isInFront = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isInFront = false;
        super.onPause();
    }

    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentById(R.id.container);
    }

    public void removeIncomeCallFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(IncomeCallFragment.class.getSimpleName());

        if (fragment != null) {
            FragmentExecuotr.removeFragment(fragmentManager, fragment);
        }
    }

    public void removeUserListFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(QbUserListFragment.class.getSimpleName());

        if (fragment != null) {
            FragmentExecuotr.removeFragment(fragmentManager, fragment);
        }
        // popbackstackFragment();
    }

    public void removeConversationFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(IncomeCallFragment.class.getSimpleName());

        if (fragment != null) {
            FragmentExecuotr.removeFragment(fragmentManager, fragment);
        }
        popbackstackFragment();
    }

    public void releaseCurrentSession() {
        this.currentSession.removeSessionnCallbacksListener(QbActivity.this);
        this.currentSession.removeSignalingCallback(QbActivity.this);
        this.currentSession = null;
    }

    public void addVideoTrackCallbacksListener(QBRTCClientVideoTracksCallbacks videoTracksCallbacks) {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(videoTracksCallbacks);
        }
    }

    public void removeRTCClientConnectionCallback(QBRTCSessionConnectionCallbacks clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.removeSessionnCallbacksListener(clientConnectionCallbacks);
        }
    }

    public void addRTCSessionUserCallback(QBRTCSessionUserCallback sessionUserCallback) {
        this.sessionUserCallback = sessionUserCallback;
    }

    public void removeRTCSessionUserCallback(QBRTCSessionUserCallback sessionUserCallback) {
        this.sessionUserCallback = null;
    }

    public void addTCClientConnectionCallback(QBRTCSessionConnectionCallbacks clientConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(clientConnectionCallbacks);
        }
    }

    public void hangUpCurrentSession() {
        ringtonePlayer.stop();
        if (getCurrentSession() != null) {
            getCurrentSession().hangUp(new HashMap<String, String>());
            popbackstackFragment();
        }
    }

    public ArrayList<QBUser> getUserList() {
        return qbUserArrayList;
    }

    public void clearUserArrayList() {

        qbUserArrayList.clear();
    }

    public void addConversationFragmentReceiveCall(final ArrayList<QBUser> qbUserArrayList, final IncomeCallFragment incomeCallFragment) {

        QBRTCSession session = getCurrentSession();

        if (getCurrentSession() != null && qbUserArrayList.size() > 0) {
            this.qbUserArrayList.addAll(qbUserArrayList);
            SettingsUtil.setSettingsStrategy(qbUserArrayList, getDefaultSharedPrefs(), this);//DataHolder.getUserNameByID(session.getCallerID())
            ConversationFragment fragment = ConversationFragment.newInstance(qbUserArrayList, "Paras", session.getConferenceType(), session.getUserInfo(), StartConversetionReason.INCOME_CALL_FOR_ACCEPTION, getCurrentSession().getSessionID());
            addFragment(fragment, incomeCallFragment);
            removeIncomeCallFragment();
        }
    }

    public SharedPreferences getDefaultSharedPrefs() {
        return sharedPref;
    }

}
