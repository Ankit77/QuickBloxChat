package com.indianic.qbchat.commanclass;

import android.os.Bundle;
import android.util.Log;

import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.utils.chat.ChatHelper;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

/**
 * This method used for user signin to quickblox server
 */
public class QbUserSignIn {

    private QbMyCallBack qbMyCallBack;
    private final String TAG = QbUserSignIn.class.getSimpleName();


    public QbUserSignIn(final QbMyCallBack qbMyCallBack) {
        this.qbMyCallBack = qbMyCallBack;

    }

    public void quickbloxUserSignIn(final String emailAddress, final String password) {

        final QBUser qbUserLocal = new QBUser();
        qbUserLocal.setEmail(emailAddress.trim());
        qbUserLocal.setPassword(password.trim());
        Log.e(TAG, "Login User Detail===> " + qbUserLocal.getEmail() + " Password==>" + qbUserLocal.getPassword());
        ChatHelper.getInstance().loginUser(qbUserLocal, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                qbUser.setPassword(qbUserLocal.getPassword());
                QbApp.getInstance().getSharedPrefsHelper().saveQbuser(qbUser);
                Log.e(TAG, "Login User fullname=====>" + qbUser.getFullName());
                loginForChat(qbUser);

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("SignInError", e.getMessage());
                qbMyCallBack.qbFailure();
            }
        });

    }

    /**
     * This method used for user login to quickblox chat service
     * @param qbUser
     */
    private void loginForChat(final QBUser qbUser) {
        ChatHelper.getInstance().loginToChat(qbUser, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Log.e(TAG, "Chat login after register");
                qbMyCallBack.qbSuccess();
                //  Toaster.shortToast("Chat connected successfully");
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "Chat login fail after register");
                loginForChat(qbUser);
            }
        });
    }
}
