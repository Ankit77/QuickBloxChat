package com.indianic.qbchat.commanclass;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBSubscription;

import java.util.ArrayList;

/**
 * Created by PG on 15/06/16.
 * This class used for user register for push notification
 */
public class QbSubscribePush {
    private QbMyCallBack qbMyCallBack;
    private Context context;

    public QbSubscribePush(QbMyCallBack qbMyCallBack, Context context) {
        this.qbMyCallBack = qbMyCallBack;
        this.context = context;
    }

    /**
     * This method used for register user GCM id to quickblox server
     * @param registrationID
     */
    public void subscribeToPushNotifications(String registrationID) {
        QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
        subscription.setEnvironment(QBEnvironment.DEVELOPMENT);
        //
        String deviceId;
        final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null) {
            deviceId = mTelephony.getDeviceId(); //*** use for mobiles
        } else {
            deviceId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID); //*** use for tablets
        }

        if (deviceId.equalsIgnoreCase("000000000000000")) {
            final String randomeNumber = String.valueOf((long) Math.floor(Math.random() * 900000000000000L) + 100000000000000L);
            subscription.setDeviceUdid(randomeNumber);
        } else {
            subscription.setDeviceUdid(deviceId);
        }

        //
        subscription.setRegistrationID(registrationID);
        //
        QBPushNotifications.createSubscription(subscription, new QBEntityCallback<ArrayList<QBSubscription>>() {

            @Override
            public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                Log.e("Subscription", "Successfully");
                qbMyCallBack.qbSuccess();
            }

            @Override
            public void onError(QBResponseException error) {
                Log.e("Subscription", "Fail" + error.getMessage());
                qbMyCallBack.qbFailure();
            }
        });
    }
}
