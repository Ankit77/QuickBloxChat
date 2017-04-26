package com.indianic.qbchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.commanclass.QbSessionNew;
import com.indianic.qbchat.utils.QbConstant;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;

import java.util.Calendar;
import java.util.Date;

/**
 * This class used for continue  check session is expireed or not
 */
public class QbsessionCheckService extends Service {
    private final int INTERVAL = 5000; //5 Second
    private final int TENMINUTE = 600000; //10 Minute
    long tokenExpiredTimeLocal = 0;
    private QbSessionNew qbSessionNew;
    private final String TAG = QbsessionCheckService.class.getSimpleName();
    private Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        qbSessionCall();
        return START_NOT_STICKY;
    }

    /**
     * Method used for creating new token
     */
    private void qbSessionCall() {
        tokenExpiredTimeLocal = QbApp.getInstance().getSharedPrefsHelper().getPrefTimeValueForSession(QbConstant.Pref_TokenCreateTime) - TENMINUTE;
        qbSessionNew = new QbSessionNew(new QbMyCallBack() {
            @Override
            public void qbSuccess() {
                tokenExpiredTimeLocal = QbApp.getInstance().getSharedPrefsHelper().getPrefTimeValueForSession(QbConstant.Pref_TokenCreateTime) - TENMINUTE;
                handler.postDelayed(runnable, INTERVAL);
            }

            @Override
            public void qbFailure() {
                checkSessionExpiredOrNot();
            }
        });
        qbSessionNew.createAndCheckSession();
        handler.postDelayed(runnable, INTERVAL);
    }

    /**
     * Method used for check token is expired or not
     */
    private void checkSessionExpiredOrNot() {
        Date expirationDate = new Date();
        long expMillis = expirationDate.getTime();
        expMillis = expMillis + QbApp.getInstance().getTimeDifference();
        expirationDate.setTime(expMillis);

        Date currentDate = new Date();
        currentDate.setTime(tokenExpiredTimeLocal);

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        Date twoHoursAfter = cal.getTime();
        Log.e(TAG, expMillis + "");
        if (!expirationDate.before(twoHoursAfter)) {
            qbSessionNew.createNewSession();
            try {
                handler.removeCallbacks(runnable);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            Log.e(TAG, "Not expired");
        }

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkSessionExpiredOrNot();
            handler.postDelayed(runnable, INTERVAL);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            handler.removeCallbacks(runnable);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }
}
