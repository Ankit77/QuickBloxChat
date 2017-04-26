package com.indianic.qbchat.commanclass;

import android.os.Bundle;

import com.indianic.qbchat.utils.chat.ChatHelper;
import com.indianic.qbchat.utils.qb.callback.QbRetriveUserCallback;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by PG on 09/06/16.
 */
public class QbRetriveAllUserFromId {
    private QbRetriveUserCallback qbRetriveAllUser;

    public QbRetriveAllUserFromId(QbRetriveUserCallback qbRetriveAllUser) {
        this.qbRetriveAllUser = qbRetriveAllUser;
    }

    public void callForUser(final ArrayList<Integer> allUserId, final QBPagedRequestBuilder pagedRequestBuilder) {
        ChatHelper.getInstance().retrieveAllUsersFromId(allUserId, pagedRequestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                qbRetriveAllUser.issuccess(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {
                qbRetriveAllUser.isfail(e);
            }
        });
    }
}
