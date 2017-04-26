package com.indianic.qbchat.commanclass;

import android.os.Bundle;
import android.util.Log;

import com.indianic.qbchat.utils.chat.ChatHelper;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

/**
 * This class used for user login to quickblox
 */
public class QbLoginForChat {

    private QbMyCallBack qbMyCallBack;
    private final String TAG = QbLoginForChat.class.getSimpleName();


    public QbLoginForChat(final QbMyCallBack qbMyCallBack) {
        this.qbMyCallBack = qbMyCallBack;
    }

    /**
     * Method used for creating user login
     * @param qbUserLocal
     */
    public void quickbloxUserSignIn(final QBUser qbUserLocal) {

        Log.e(TAG, "Login User Detail===> " + qbUserLocal.getEmail() + " Password==>" + qbUserLocal.getPassword());
        ChatHelper.getInstance().loginUser(qbUserLocal, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.e(TAG, "Login User fullname=====>" + qbUser.getFullName());
                loginForChat(qbUserLocal);

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("SignInError", e.getMessage());
                qbMyCallBack.qbFailure();
            }
        });

    }

    /**
     * Method used for user login to quickblox chat service
     * @param qbUser
     */
    public void loginForChat(final QBUser qbUser) {

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
