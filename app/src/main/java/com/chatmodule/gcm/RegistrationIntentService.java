package com.chatmodule.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.chatmodule.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.utils.QbConstant;


/**
 * Purpose:-
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            final InstanceID instanceID = InstanceID.getInstance(this);
            final String gcmToken = instanceID.getToken(getResources().getString(R.string.gcmprojectkey), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.e("GCM TOKEN:-", gcmToken);
            // preferenceUtils.savePreferenceData(preferenceUtils.KEY_DEVICE_TOKEN, gcmToken);
            QbApp.getInstance().getSharedPrefsHelper().saveToPrefrance(QbConstant.Pref_GCMToken, gcmToken);
        } catch (Exception e) {
        }
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param registrationID The new token.
     */


}
