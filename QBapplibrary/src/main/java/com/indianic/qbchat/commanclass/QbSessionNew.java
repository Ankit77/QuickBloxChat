package com.indianic.qbchat.commanclass;

import android.os.Bundle;
import android.util.Log;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.utils.QbConstant;
import com.indianic.qbchat.utils.chat.ChatHelper;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.BaseService;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by PG on 09/06/16.
 * This class used for creating new token for quickblox api call
 */
public class QbSessionNew {
    private static final String TAG = QbSessionNew.class.getSimpleName();
    private QbMyCallBack qbMyCallBack;

    public QbSessionNew(QbMyCallBack qbMyCallBack) {
        this.qbMyCallBack = qbMyCallBack;
    }

    public void createAndCheckSession() {
        final long tokenExpiredTimeLocal = QbApp.getInstance().getSharedPrefsHelper().getPrefTimeValueForSession(QbConstant.Pref_TokenCreateTime);
        final String oldToken = QbApp.getInstance().getSharedPrefsHelper().getPrefValue(QbConstant.Pref_Token, "");

        final Date expirationDate = validateTime(tokenExpiredTimeLocal);
        if (expirationDate != null) {
            try {
                QBAuth.createFromExistentToken(oldToken, expirationDate);
                qbMyCallBack.qbSuccess();
            } catch (Exception e) {
                createAndCheckSession();
            }
        } else {
            createNewSession();
        }
    }

    /**
     * This method used to check date for token
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
     * Method used to create new token
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
