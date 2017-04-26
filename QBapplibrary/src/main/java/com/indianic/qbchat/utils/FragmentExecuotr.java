package com.indianic.qbchat.utils;

import android.app.Fragment;
import android.app.FragmentManager;

/**
 * QuickBlox team
 */
public class FragmentExecuotr {

    public static void addFragment(FragmentManager fragmentManager, int containerId, Fragment fragment, String tag) {
        fragmentManager.beginTransaction().replace(containerId, fragment, tag).commitAllowingStateLoss();
    }
    public static void addIncomingFragment(FragmentManager fragmentManager, int containerId, Fragment fragment, String tag) {
        fragmentManager.beginTransaction().add(containerId, fragment, tag).addToBackStack(tag).commit();
    }
    public static void removeFragment(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction().remove(fragment).commit();
    }
}
