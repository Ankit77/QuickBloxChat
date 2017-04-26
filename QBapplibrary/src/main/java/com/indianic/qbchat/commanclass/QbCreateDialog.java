package com.indianic.qbchat.commanclass;

import android.os.Bundle;

import com.indianic.qbchat.utils.chat.ChatHelper;
import com.indianic.qbchat.utils.qb.callback.QbDialogCreateCallBack;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.util.List;

/**
 * Created by PG on 09/06/16.
 * This class used for creting new dialog for chat
 */
public class QbCreateDialog {
    private QbDialogCreateCallBack qbDialogCreateCallBack;

    public QbCreateDialog(QbDialogCreateCallBack qbDialogCreateCallBack) {
        this.qbDialogCreateCallBack = qbDialogCreateCallBack;
    }

    /**
     * Method used for creating new quickblox dialog
     * @param qbUserList
     * @param isAction
     * @param groupName
     */
    public void createDialog(final List<QBUser> qbUserList,final int isAction,final String groupName) {
        ChatHelper.getInstance().createDialogWithSelectedUsers(qbUserList,isAction,groupName, new QBEntityCallback<QBDialog>() {
            @Override
            public void onSuccess(QBDialog qbnewDialog, Bundle bundle) {
                qbDialogCreateCallBack.issuccess(qbnewDialog);
            }

            @Override
            public void onError(QBResponseException e) {
                qbDialogCreateCallBack.isfail(e);
            }
        });
    }
}
