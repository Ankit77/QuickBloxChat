package com.indianic.qbchat.utils;

/**
 * Created by indianic on 17/05/16.
 */
public class CommanUtil {

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

}
