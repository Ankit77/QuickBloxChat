package com.chatmodule;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.utils.SharedPrefsHelper;

/**
 * Created by indianic on 09/06/16.
 */
public class AppChat extends QbApp {
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        sharedPrefsHelper = new SharedPrefsHelper(this, getPackageName().toString());
        setTime();
        initCredentials();

    }


}
