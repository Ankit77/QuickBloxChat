package com.indianic.qbchat.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.indianic.qbchat.R;
import com.indianic.qbchat.activity.QbActivity;
import com.indianic.qbchat.commanclass.QbUserSignIn;
import com.indianic.qbchat.utils.CommanUtil;
import com.indianic.qbchat.utils.ConnectivityUtils;
import com.indianic.qbchat.utils.ErrorUtils;
import com.indianic.qbchat.utils.KeyboardUtils;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;

/**
 * Created by PG on 24/05/16.
 */
public class QbSigninFragment extends QbBaseFragment {
    private View viewsnakbar;
    private EditText edt_EmailAddress;
    private EditText edt_Password;
    private Button btn_signin;
    private Button btn_signup;
    Snackbar snackbar = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.qbfragment_signin, container, false);
    }

    @Override
    public void initView(View view) {
        ((QbActivity) getActivity()).setToolBarTitle(getString(R.string.string_signin));
        viewsnakbar = _findViewById(R.id.activity_signin_linearMain, view);
        edt_EmailAddress = _findViewById(R.id.activity_signin_edt_emailaddress, view);
        edt_Password = _findViewById(R.id.activity_signin_edt_password, view);
        btn_signin = _findViewById(R.id.activity_signin_btn_signin, view);
        btn_signup = _findViewById(R.id.activity_signin_btn_signup, view);
        btn_signup.setOnClickListener(this);
        btn_signin.setOnClickListener(this);
    }

    @Override
    public void initActionBar() {

    }

    @Override
    public void onClick(View v) {
        KeyboardUtils.hideKeyboard(edt_Password);
        int i = v.getId();
        if (i == R.id.activity_signin_btn_signin) {
            if (snackbar != null) {
                snackbar.dismiss();
            }
            validationCheck();

        } else if (i == R.id.activity_signin_btn_signup) {
            ((QbActivity) getActivity()).addFragment(new QbSignupFragment(), this);

        }
    }

    private void validationCheck() {
        if (TextUtils.isEmpty(edt_EmailAddress.getText().toString().trim())) {
            edt_EmailAddress.setError(getString(R.string.err_email));
            edt_EmailAddress.setFocusable(true);
            return;

        } else if (!CommanUtil.isValidEmail(edt_EmailAddress.getText().toString().trim())) {
            edt_EmailAddress.setError(getString(R.string.err_email_valid));
            edt_EmailAddress.setFocusable(true);
            return;
        } else if (TextUtils.isEmpty(edt_Password.getText().toString().trim())) {
            edt_Password.setError(getString(R.string.err_password));
            edt_Password.setFocusable(true);
            return;

        } else {
            if (ConnectivityUtils.isNetworkConnected()) {
                quickbloxSignIn();
            } else {
                snackbar = ErrorUtils.showSnackbar(viewsnakbar, R.string.no_internet_connection, R.string.dlg_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        validationCheck();
                    }
                });
            }

        }
    }

    /**
     * Method used for user signin
     */
    private void quickbloxSignIn() {
        progressDialog.show();
        final QbUserSignIn qbLogin = new QbUserSignIn(new QbMyCallBack() {
            @Override
            public void qbSuccess() {
                progressDialog.cancel();
                ((QbActivity) getActivity()).replaceFragment(new QbMessagesFragment());
                ((QbActivity)getActivity()).initvideocall();
            }

            @Override
            public void qbFailure() {
                progressDialog.cancel();
                Toast.makeText(getActivity(), getString(R.string.string_invalid_email_pass), Toast.LENGTH_SHORT).show();
            }
        });
        qbLogin.quickbloxUserSignIn(edt_EmailAddress.getText().toString().trim(), edt_Password.getText().toString().trim());

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        ((QbActivity) getActivity()).setToolBarTitle(getString(R.string.string_signin));

    }
}
