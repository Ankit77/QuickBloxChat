package com.indianic.qbchat.utils.qb;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;


import com.indianic.qbchat.QbApp;
import com.indianic.qbchat.R;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

public class VerboseQbChatConnectionListener implements ConnectionListener {
    private static final String TAG = VerboseQbChatConnectionListener.class.getSimpleName();
    private View rootView;
    private Snackbar snackbar;

    public VerboseQbChatConnectionListener(View rootView) {
        this.rootView = rootView;
    }

    @Override
    public void connected(XMPPConnection connection) {
        Log.i(TAG, "connected()");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean authenticated) {
        Log.e(TAG, "authenticated()");
    }

    @Override
    public void connectionClosed() {
        Log.i(TAG, "connectionClosed()");
    }

    @Override
    public void connectionClosedOnError(final Exception e) {
        Log.e(TAG, "connectionClosedOnError(): " + e.getLocalizedMessage());
        snackbar = Snackbar.make(rootView, QbApp.getInstance().getString(R.string.connection_error), Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    @Override
    public void reconnectingIn(final int seconds) {
        if (seconds % 5 == 0 && seconds != 0) {
            Log.i(TAG, "reconnectingIn(): " + seconds);
            snackbar = Snackbar.make(rootView, QbApp.getInstance().getString(R.string.reconnect_alert, seconds), Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    @Override
    public void reconnectionSuccessful() {
        Log.e(TAG, "reconnectionSuccessful()");
        snackbar.dismiss();
    }

    @Override
    public void reconnectionFailed(final Exception error) {
        Log.e(TAG, "reconnectionFailed(): " + error.getLocalizedMessage());
    }
}
