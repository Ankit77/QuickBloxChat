package com.indianic.qbchat.utils.qb.callback;

import com.quickblox.chat.model.QBDialog;

/**
 * Created by indianic on 09/06/16.
 */
public interface QbDialogCreateCallBack {
    public void issuccess(QBDialog qbDialog);
    public void isfail(Exception e);
}
