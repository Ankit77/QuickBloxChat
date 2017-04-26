package com.indianic.qbchat.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.indianic.qbchat.R;
import com.indianic.qbchat.activity.QbActivity;
import com.indianic.qbchat.commanclass.QbRetriveAllUserFromId;
import com.indianic.qbchat.utils.QbConstant;
import com.indianic.qbchat.utils.RingtonePlayer;
import com.indianic.qbchat.utils.qb.callback.QbRetriveUserCallback;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * QuickBlox team
 */
public class IncomeCallFragment extends Fragment implements Serializable, View.OnClickListener {

    private static final String TAG = IncomeCallFragment.class.getSimpleName();
    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);
    private TextView incVideoCall;
    private TextView incAudioCall;
    private TextView callerName;
    private TextView otherIncUsers;
    private TextView alsoOnCall;
    private ImageButton rejectBtn;
    private ImageButton takeBtn;

    private ArrayList<Integer> opponents;
    private List<QBUser> opponentsFromCall = new ArrayList<>();
    private QBRTCSessionDescription sessionDescription;
    private Vibrator vibrator;
    private QBRTCTypes.QBConferenceType conferenceType;
    private int qbConferenceType;
    private View view;
    private long lastCliclTime = 0l;
    private RingtonePlayer ringtonePlayer;
    private ArrayList<QBUser> qbUserArrayList;
    private Map<String,String> stringUserInfoMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getArguments() != null) {
            ((QbActivity)getActivity()).clearUserArrayList();
            qbUserArrayList = new ArrayList<>();
            opponents = getArguments().getIntegerArrayList("opponents");
            stringUserInfoMap= (Map<String, String>) getArguments().get(QbConstant.USERINFO);
            loadUsersFromQb();
            sessionDescription = (QBRTCSessionDescription) getArguments().getSerializable("sessionDescription");
            qbConferenceType = getArguments().getInt(QbConstant.CONFERENCE_TYPE);


            conferenceType =
                    qbConferenceType == 1 ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO :
                            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

            Log.d(TAG, conferenceType.toString() + "From onCreateView()");
        }

        if (savedInstanceState == null) {

            view = inflater.inflate(R.layout.fragment_income_call, container, false);
            initUI(view);
            setDisplayedTypeCall(conferenceType);
            initButtonsListener();

        }
        ringtonePlayer = new RingtonePlayer(getActivity());

        return view;
    }

    /**
     * Method used for geting list of registed user from quickblox server
     */
    private void loadUsersFromQb() {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(100);
        QbRetriveAllUserFromId qbRetriveAllUserFromId = new QbRetriveAllUserFromId(new QbRetriveUserCallback() {
            @Override
            public void issuccess(ArrayList<QBUser> qbUsersList) {
                qbUserArrayList.addAll(qbUsersList);
                Log.e("qbUserArrayList",qbUserArrayList.size()+"");

            }

            @Override
            public void isfail(Exception e) {
                loadUsersFromQb();

            }
        });
        qbRetriveAllUserFromId.callForUser(opponents, pagedRequestBuilder);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);

        Log.d(TAG, "onCreate() from IncomeCallFragment");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        startCallNotification();
    }

    private void initButtonsListener() {
        rejectBtn.setOnClickListener(this);
        takeBtn.setOnClickListener(this);
    }

    private void initUI(View view) {

        incAudioCall = (TextView) view.findViewById(R.id.incAudioCall);
        incVideoCall = (TextView) view.findViewById(R.id.incVideoCall);

        callerName = (TextView) view.findViewById(R.id.callerName);
        callerName.setText(stringUserInfoMap.get(QbConstant.CALLERQUICKBLOXNAME)+"");
        // callerName.setBackgroundResource(ListUsersActivity.selectBackgrounForOpponent((DataHolder.getUserIndexByID((((CallActivity) getActivity()).getCurrentSession().getCallerID()))) + 1));

        otherIncUsers = (TextView) view.findViewById(R.id.otherIncUsers);
        alsoOnCall= (TextView) view.findViewById(R.id.alsoOnCall);
        if(stringUserInfoMap.get(QbConstant.ONCALLETOTALUSER)!=null)
        {
            int totalUser=Integer.parseInt(stringUserInfoMap.get(QbConstant.ONCALLETOTALUSER));
            if(totalUser>1)
            {
                otherIncUsers.setText(stringUserInfoMap.get(QbConstant.USERONCALL));
            }
            else
            {
                alsoOnCall.setVisibility(View.GONE);
                otherIncUsers.setVisibility(View.GONE);
            }
        }


        rejectBtn = (ImageButton) view.findViewById(R.id.rejectBtn);
        takeBtn = (ImageButton) view.findViewById(R.id.takeBtn);
    }

    private void enableButtons(boolean enable) {
        takeBtn.setEnabled(enable);
        rejectBtn.setEnabled(enable);
    }

    public void startCallNotification() {

        ringtonePlayer.play(false);

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        long[] vibrationCycle = {0, 1000, 1000};
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationCycle, 1);
        }

    }

    private void stopCallNotification() {
        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private String getOtherIncUsersNames(ArrayList<Integer> opponents) {
        StringBuffer s = new StringBuffer("");
        opponents.remove(QBChatService.getInstance().getUser().getId());

        for (Integer i : opponents) {
            for (QBUser usr : opponentsFromCall) {
                if (usr.getId().equals(i)) {
                    if (opponents.indexOf(i) == (opponents.size() - 1)) {
                        s.append(usr.getFullName() + " ");
                        break;
                    } else {
                        s.append(usr.getFullName() + ", ");
                    }
                }
            }
        }
        return s.toString();
    }

    private String getCallerName(QBRTCSession session) {
        String s = new String();
        int i = session.getCallerID();

        //  opponentsFromCall.addAll(DataHolder.getUsers());

//        for (QBUser usr : opponentsFromCall) {
//            if (usr.getId().equals(i)) {
//                s = usr.getFullName();
//            }
//        }
        return s;
    }

    private void setDisplayedTypeCall(QBRTCTypes.QBConferenceType conferenceType) {
        if (conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO) {
            incVideoCall.setVisibility(View.VISIBLE);
            incAudioCall.setVisibility(View.INVISIBLE);
        } else if (conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO) {
            incVideoCall.setVisibility(View.INVISIBLE);
            incAudioCall.setVisibility(View.VISIBLE);
        }
    }

    public void onStop() {
        stopCallNotification();
        super.onDestroy();
        Log.d(TAG, "onDestroy() from IncomeCallFragment");
    }

    @Override
    public void onClick(View v) {

        if ((SystemClock.uptimeMillis() - lastCliclTime) < CLICK_DELAY) {
            return;
        }
        lastCliclTime = SystemClock.uptimeMillis();
        int i = v.getId();
        if (i == R.id.rejectBtn) {
            reject();

        } else if (i == R.id.takeBtn) {
            accept();

        } else {
        }
    }

    ;

    private void accept() {
        takeBtn.setClickable(false);
        stopCallNotification();

        ((QbActivity) getActivity()).addConversationFragmentReceiveCall(qbUserArrayList,this);
        qbUserArrayList.clear();
        Log.d(TAG, "Call is started");
    }

    private void reject() {
        rejectBtn.setClickable(false);
        Log.d(TAG, "Call is rejected");

        stopCallNotification();

        ((QbActivity) getActivity()).rejectCurrentSession();
        ((QbActivity) getActivity()).removeIncomeCallFragment();
        //((QbActivity) getActivity()).addOpponentsFragment();
    }

}
