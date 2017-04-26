package com.chatmodule.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.chatmodule.AppChat;
import com.chatmodule.R;
import com.chatmodule.gcm.RegistrationIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.indianic.qbchat.activity.QbActivity;
import com.indianic.qbchat.commanclass.QbLoginForChat;
import com.indianic.qbchat.commanclass.QbSessionNew;
import com.indianic.qbchat.utils.DialogUtils;
import com.indianic.qbchat.utils.QbConstant;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;
import com.quickblox.users.model.QBUser;

public class SplashActivity extends AppCompatActivity {
    protected ProgressDialog progressDialog;
    QbLoginForChat qbLoginForChat;
    QbSessionNew qbSessionNew;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            final Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        init();
    }

    private void init() {
        progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        qbSessionNew = new QbSessionNew(new QbMyCallBack() {
            @Override
            public void qbSuccess() {
                //Toast.makeText(SplashActivity.this, "Session Success fully created", Toast.LENGTH_SHORT).show();
                final QBUser qbUserTmp = AppChat.getInstance().getSharedPrefsHelper().getQbUser();
                if (qbUserTmp == null) {
                    signinUser();
                    progressDialog.cancel();
                    finish();

                } else {
                    Log.e("QbDetail", qbUserTmp.getEmail() + "===" + qbUserTmp.getPassword() + "====" + qbUserTmp.getId());
                    qbLoginForChat = new QbLoginForChat(new QbMyCallBack() {
                        @Override
                        public void qbSuccess() {
                            viewOldmessage();
                            progressDialog.cancel();
                            finish();
                        }

                        @Override
                        public void qbFailure() {
                            qbLoginForChat.quickbloxUserSignIn(qbUserTmp);
                        }
                    });
                    qbLoginForChat.quickbloxUserSignIn(qbUserTmp);
                }

            }

            @Override
            public void qbFailure() {
                Toast.makeText(SplashActivity.this, "Session created fail", Toast.LENGTH_SHORT).show();
            }
        });
        qbSessionNew.createAndCheckSession();
    }

    public void signinUser() {
        Intent mIntent = new Intent(this, QbActivity.class);
        mIntent.putExtra(QbActivity.isActionType, QbConstant.isActionSignIn);
        startActivity(mIntent);
    }

    public void viewOldmessage() {
        Intent mIntent = new Intent(this, QbActivity.class);
        mIntent.putExtra(QbActivity.isActionType, QbConstant.isActionVieOldMessage);
        startActivity(mIntent);
    }
    private boolean checkPlayServices() {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.e("Play service error", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
