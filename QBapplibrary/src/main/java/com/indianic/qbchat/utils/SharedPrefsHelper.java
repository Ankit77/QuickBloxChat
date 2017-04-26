package com.indianic.qbchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.quickblox.users.model.QBUser;

/**
 * Created by PG on 17/05/16.
 */
public class SharedPrefsHelper {

    SharedPreferences sharedPreferences;
private final String TAG=SharedPrefsHelper.class.getSimpleName();
    public SharedPrefsHelper(Context context,String prefranceName) {
        sharedPreferences = context.getSharedPreferences(prefranceName, Context.MODE_PRIVATE);

    }

    public long getPrefTimeValueForSession(String key)
    {
        if(hasValue(key)) {
            return sharedPreferences.getLong(key, 0);
        }
        else
        {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getPrefValue(String key) {
        return (T) sharedPreferences.getAll().get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPrefValue(String key, T defValue) {
        T returnValue = (T) sharedPreferences.getAll().get(key);
        return returnValue == null ? defValue : returnValue;
    }

    public void saveToPrefrance(String key, Object value) {
        SharedPreferences.Editor editor = getEditor();
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            Log.e(TAG,"Save as Integer");
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            Log.e(TAG,"Save as Flot");
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            Log.e(TAG,"Save as Long");
            editor.putLong(key, (Long) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Enum) {
            editor.putString(key, value.toString());
        } else if (value != null) {
            throw new RuntimeException("Attempting to save non-supported preference");
        }
        editor.apply();
    }

    public boolean hasValue(String key) {
        return sharedPreferences.contains(key);
    }

    public void delete(String key) {
        if (sharedPreferences.contains(key)) {
            getEditor().remove(key).commit();
        }
    }

    private SharedPreferences.Editor getEditor() {
        return sharedPreferences.edit();
    }


    public QBUser getQbUser() {

        if (hasValue(QbConstant.Pref_UserEmailAddress) && hasValue(QbConstant.Pref_UserPassword)) {
            Integer id = getPrefValue(QbConstant.Pref_UserQuickBloxId, 0);
            String emailAddress = getPrefValue(QbConstant.Pref_UserEmailAddress, "");
            String password = getPrefValue(QbConstant.Pref_UserPassword, "");
            String fullName = getPrefValue(QbConstant.Pref_UserFullName);

            QBUser user = new QBUser();
            user.setId(id);
            user.setEmail(emailAddress);
            user.setPassword(password);
            user.setFullName(fullName);
            return user;
        } else {
            return null;
        }
    }

    public void saveQbuser(QBUser qbUser)
    {
        saveToPrefrance(QbConstant.Pref_UserQuickBloxId,qbUser.getId());
        saveToPrefrance(QbConstant.Pref_UserEmailAddress,qbUser.getEmail());
        saveToPrefrance(QbConstant.Pref_UserPassword,qbUser.getPassword());
        saveToPrefrance(QbConstant.Pref_UserFullName,qbUser.getFullName());
    }

}
