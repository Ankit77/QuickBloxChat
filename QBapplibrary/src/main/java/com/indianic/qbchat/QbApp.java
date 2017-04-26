package com.indianic.qbchat;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.indianic.qbchat.utils.QbConstant;
import com.indianic.qbchat.utils.SharedPrefsHelper;
import com.indianic.qbchat.utils.SntpUtils;
import com.quickblox.core.QBSettings;

/**
 * Created by PG on 17/05/16.
 */
public class QbApp extends Application {

    public static QbApp app;
    public SharedPrefsHelper sharedPrefsHelper;
    private long serverTimeUTC = 0;
    private long timeDifference = 0;

    public static synchronized QbApp getInstance() {
        return app;
    }

    public SharedPrefsHelper getSharedPrefsHelper() {
        return sharedPrefsHelper;
    }

    public void initCredentials() {
        QBSettings.getInstance().init(getApplicationContext(), QbConstant.QB_APP_ID, QbConstant.QB_AUTH_KEY, QbConstant.QB_AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(QbConstant.QB_ACCOUNT_KEY);

    }

    public void setTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("LOG", "requesting time..");

                //Date dt = SntpUtils.getUTCDate();
                // Log.e("LOG", "dt: " + dt.toString());

                serverTimeUTC = SntpUtils.getUTCTimestamp();
                Log.e("LOG", "serverTimeUTC: " + serverTimeUTC);


                long mySystemTime = System.currentTimeMillis();

                Log.e("mySystemTime", System.currentTimeMillis() + "");
                timeDifference = serverTimeUTC - mySystemTime;
                Log.e("difrent", timeDifference + "");
               // startService(new Intent(QbApp.this, QbLoginRegisterService.class));

            }
        }).start();
    }

    public long getTimeDifference() {
        return timeDifference;
    }

    public long getServerTimeUTC() {
        return serverTimeUTC;
    }


}
