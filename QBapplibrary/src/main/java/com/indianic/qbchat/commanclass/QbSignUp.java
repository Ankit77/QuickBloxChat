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
 * This class used for user register to quickblox server
 */
public class QbSignUp {

    private QbMyCallBack qbMyCallBack;


    public QbSignUp(QbMyCallBack qbMyCallBack) {
        this.qbMyCallBack = qbMyCallBack;
    }

    /**
     * Method used for register user to Quickblox
     * @param fullNameResult
     * @param emailAddressResult
     * @param passwordResult
     */
    public void quickbloxUserSignUp(final String fullNameResult, final String emailAddressResult, final String passwordResult) {

        final QBUser qbUserLocal = new QBUser();
        qbUserLocal.setFullName(fullNameResult.trim());
        qbUserLocal.setEmail(emailAddressResult.trim());
        qbUserLocal.setPassword(passwordResult.trim());
        ChatHelper.getInstance().signUpUser(qbUserLocal, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                qbUser.setPassword(qbUserLocal.getPassword());
                QbApp.getInstance().getSharedPrefsHelper().saveQbuser(qbUser);
                loginForChat(qbUser);

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("SignUp", e.getMessage() + "");
                qbMyCallBack.qbFailure();
            }
        });
    }

    /**
     * This method used for user login for quickblox chat service
     * @param qbUser
     */
    private void loginForChat(final QBUser qbUser) {
        ChatHelper.getInstance().loginToChat(qbUser, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                qbMyCallBack.qbSuccess();
            }

            @Override
            public void onError(QBResponseException e) {
                qbMyCallBack.qbFailure();
            }
        });
    }


}
