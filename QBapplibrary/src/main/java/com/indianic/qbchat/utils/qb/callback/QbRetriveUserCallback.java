package com.indianic.qbchat.utils.qb.callback;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by indianic on 09/06/16.
 */
public interface QbRetriveUserCallback {
   public void issuccess(ArrayList<QBUser> qbUsers);
   public void isfail(Exception e);
}
