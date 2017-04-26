package com.indianic.qbchat.commanclass;


import android.os.Bundle;
import android.util.Log;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.utils.QbConstant;
import com.indianic.qbchat.utils.chat.ChatHelper;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.BaseService;
import com.quickblox.users.model.QBUser;

import java.util.Calendar;
import java.util.Date;

/**
 * This class used for creating session (Token create for quickblox api)
 */
public class QbSession {

    private static final String TAG = QbSession.class.getSimpleName();
    private QbMyCallBack qbMyCallBack;


    public QbSession(QbMyCallBack qbMyCallBack) {
        this.qbMyCallBack = qbMyCallBack;
    }

    /**
     * This method used for creating quickblox session
     */
    public void createSession() {

        final QBUser qbUserLocal = QbApp.getInstance().getSharedPrefsHelper().getQbUser();
        if (qbUserLocal == null) {
            createNewSession();
        } else {
            final long tokenExpiredTimeLocal = QbApp.getInstance().getSharedPrefsHelper().getPrefTimeValueForSession(QbConstant.Pref_TokenCreateTime);
            final String oldToken = QbApp.getInstance().getSharedPrefsHelper().getPrefValue(QbConstant.Pref_Token, "");

            final Date expirationDate = validateTime(tokenExpiredTimeLocal);
            if (expirationDate != null) {
                ChatHelper.getInstance().loginForChatwithOldToken(qbUserLocal, oldToken, expirationDate, new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        // Call success method
                        qbMyCallBack.qbSuccess();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        createSession();
                    }
                });
            } else {
                createLoginSession(qbUserLocal);
            }
        }
    }

    /**
     * Method used for creating session and and loginto user for chat
     * @param qbUserLocal
     */

    private void createLoginSession(final QBUser qbUserLocal) {
        ChatHelper.getInstance().loginForChatwithNewToken(qbUserLocal, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                String token = null;
                Date expirationDate = null;
                try {
                    token = BaseService.getBaseService().getToken();
                    expirationDate = BaseService.getBaseService().getTokenExpirationDate();
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }


                Date currentDate = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDate);
                cal.add(Calendar.HOUR, 2);
                Date twoHoursAfter = cal.getTime();
                long newCreatedTime = twoHoursAfter.getTime() + QbApp.getInstance().getTimeDifference();

                QbApp.getInstance().getSharedPrefsHelper().saveToPrefrance(QbConstant.Pref_TokenCreateTime, newCreatedTime);
                QbApp.getInstance().getSharedPrefsHelper().saveToPrefrance(QbConstant.Pref_TokenExpiredTime, expirationDate.getTime());
                QbApp.getInstance().getSharedPrefsHelper().saveToPrefrance(QbConstant.Pref_Token, token);
                Log.e(TAG, "User available NewSession ==> Success with chat service");
                // Call success method
                qbMyCallBack.qbSuccess();
            }

            @Override
            public void onError(QBResponseException e) {
                createLoginSession(qbUserLocal);

            }
        });
    }

    /**
     * Method check session is expired or not (Session token valid for 2 Hours)
     * @param tokenExpiredTimeLocal
     * @return
     */
    private Date validateTime(final long tokenExpiredTimeLocal) {
        Date expirationDate = new Date();
        long expMillis = expirationDate.getTime();
        expMillis = expMillis + QbApp.getInstance().getTimeDifference();
        expirationDate.setTime(expMillis);

        Date currentDate = new Date();
        currentDate.setTime(tokenExpiredTimeLocal);
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);

        Date twoHoursAfter = cal.getTime();

        if (expirationDate.before(twoHoursAfter)) {
            Log.e("QbSession", "Oldsession");
            return expirationDate;

        } else {
            Log.e("QbSession", "New Session");
            return null;
        }
    }

    /**
     * Method used for creating new token for quickblox api calling
     */
    public void createNewSession() {
        try {

            ChatHelper.getInstance().newSessionCreate(new QBEntityCallback<QBSession>() {
                @Override
                public void onSuccess(QBSession qbSession, Bundle bundle) {
                    Log.e(TAG, qbSession.getTimestamp() + "=====>" + qbSession.getNonce());

                    String token = null;
                    Date expirationDate = null;
                    try {
                        token = BaseService.getBaseService().getToken();
                        expirationDate = BaseService.getBaseService().getTokenExpirationDate();
                    } catch (BaseServiceException e) {
                        e.printStackTrace();
                    }


                    Date currentDate = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(currentDate);
                    cal.add(Calendar.HOUR, 2);
                    Date twoHoursAfter = cal.getTime();
                    long newCreatedTime = twoHoursAfter.getTime() + QbApp.getInstance().getTimeDifference();
                    QbApp.getInstance().getSharedPrefsHelper().saveToPrefrance(QbConstant.Pref_TokenCreateTime, newCreatedTime);
                    QbApp.getInstance().getSharedPrefsHelper().saveToPrefrance(QbConstant.Pref_TokenExpiredTime, expirationDate.getTime());
                    QbApp.getInstance().getSharedPrefsHelper().saveToPrefrance(QbConstant.Pref_Token, token);

                    // call success method
                    qbMyCallBack.qbSuccess();
                }

                @Override
                public void onError(QBResponseException e) {
                    createNewSession();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "New Token create Error==>" + e.getMessage());
        }
    }
}
