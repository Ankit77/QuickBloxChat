package com.indianic.qbchat.commanclass;

import android.os.Bundle;
import android.util.Log;

import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBPushType;

/**
 * Created by PG on 15/06/16.
 * This class used for creating broadcast
 */
public class QbBroadcastSend {
    private final String TAG=QbBroadcastSend.class.getSimpleName();
    private QbMyCallBack qbMyCallBack;

    public QbBroadcastSend(QbMyCallBack qbMyCallBack) {
        this.qbMyCallBack = qbMyCallBack;
    }

    /**
     * Method used for sent push notification
     * @param userIds
     * @param data
     */
    public void sendPush(final StringifyArrayList<Integer> userIds, final String data) {
        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setPushType(QBPushType.GCM);
        event.setMessage(data);

        QBPushNotifications.createEvent(event, new QBEntityCallback<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle args) {
                qbMyCallBack.qbSuccess();
                // sent
            }

            @Override
            public void onError(QBResponseException errors) {
                Log.e(TAG,errors.getMessage());
                qbMyCallBack.qbFailure();

            }
        });
    }
}
