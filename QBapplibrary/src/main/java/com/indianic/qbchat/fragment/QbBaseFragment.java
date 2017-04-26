package com.indianic.qbchat.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.indianic.qbchat.utils.DialogUtils;


/**
 * Created on 30/01/16.
 */
public abstract class QbBaseFragment extends Fragment implements View.OnClickListener {
    private long mLastClickTime = 0;
    protected ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return super.onCreateView(inflater, container, savedInstanceState);

    }

    public abstract void initView(View view);


    public abstract void initActionBar();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressDialog = DialogUtils.getProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        initView(view);

    }

    @SuppressWarnings("unchecked")
    public <T extends View> T _findViewById(int viewId, View view) {
        return (T) view.findViewById(viewId);
    }

    @Override
    public void onClick(View v) {
        // Utils.hideSoftKeyboard(getActivity());
        /**
         * Logic to Prevent the Launch of the Fragment Twice if User makes
         * the Tap(Click) very Fast.
         */
//        if (SystemClock.elapsedRealtime() - mLastClickTime < Constants.MAX_CLICK_INTERVAL) {
//            return;
//        }
//        mLastClickTime = SystemClock.elapsedRealtime();

    }
}
