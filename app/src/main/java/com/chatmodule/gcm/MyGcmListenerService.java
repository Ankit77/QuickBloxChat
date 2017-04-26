package com.chatmodule.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.chatmodule.R;
import com.chatmodule.ui.SplashActivity;
import com.google.android.gms.gcm.GcmListenerService;
import com.indianic.qbchat.QbApp;

import org.json.JSONObject;

/**
 * Purpose:- GCM service to receive push notification send by server and generate notification.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = MyGcmListenerService.class.getSimpleName();


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        final String message = data.getString("message");
        Log.e("MessageGcm", message);
        if (QbApp.getInstance().getSharedPrefsHelper().getQbUser() != null) {
            sendNotification(message);
        }
    }

    private void sendNotification(final String message) {
        String pushMessage = "", broadcastcreatedId = "", broadcastcreatedName = "", messageType = "";
        try {
            JSONObject jsonObject = new JSONObject(message);
            pushMessage = jsonObject.getString("message");
            messageType = jsonObject.getString("messageType");
            broadcastcreatedId = jsonObject.getString("broadcastcreatedId");
            broadcastcreatedName = jsonObject.getString("broadcastcreatedName");



        int notificationId = (int) System.currentTimeMillis();
        NotificationCompat.Builder mBuilder = null;
        mBuilder = new NotificationCompat.Builder(this).setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(messageType)
                .setWhen(System.currentTimeMillis()).setDefaults(Notification.DEFAULT_SOUND).setLights(Color.GREEN, 500, 1000).setContentText(pushMessage).setStyle(new NotificationCompat.BigTextStyle().bigText(pushMessage));
        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final Intent notificationIntent = new Intent(MyGcmListenerService.this, SplashActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setSmallIcon(getNotificationIcon());
        mNotificationManager.notify(notificationId, mBuilder.build());
        } catch (Exception e) {
            Log.e("PushError", e.getMessage());
        }
    }

    private int getNotificationIcon() {
        return R.mipmap.ic_launcher;
    }


}
