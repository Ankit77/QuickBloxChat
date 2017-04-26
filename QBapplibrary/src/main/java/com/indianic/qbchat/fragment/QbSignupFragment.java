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
import com.indianic.qbchat.commanclass.QbSignUp;
import com.indianic.qbchat.utils.CommanUtil;
import com.indianic.qbchat.utils.ConnectivityUtils;
import com.indianic.qbchat.utils.ErrorUtils;
import com.indianic.qbchat.utils.KeyboardUtils;
import com.indianic.qbchat.utils.qb.callback.QbMyCallBack;


/**
 * Created by PG on 24/05/16.
 */
public class QbSignupFragment extends QbBaseFragment {
    private EditText edt_FullName;
    private EditText edt_EmailAddress;
    private EditText edt_Password;
    private Button btn_signup;
    Snackbar snackbar;
    private View viewsnakbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.qbfragment_signup, container, false);
    }

    @Override
    public void initView(View view) {
        ((QbActivity) getActivity()).setToolBarTitle(getString(R.string.signup));
        viewsnakbar = _findViewById(R.id.activity_signup_linearMain, view);
        edt_FullName = _findViewById(R.id.activity_signup_edt_fullname, view);
        edt_EmailAddress = _findViewById(R.id.activity_signup_edt_emailaddress, view);
        edt_Password = _findViewById(R.id.activity_signup_edt_password, view);
        btn_signup = _findViewById(R.id.activity_signup_btn_signup, view);
        btn_signup.setOnClickListener(this);
    }

    @Override
    public void initActionBar() {

    }
//    password required minimum 8 character
//    - password required maximum 40 character
//    - full name required minimum 3 character
//    - full name required maximum 50 character
    private void validationCheck() {
        if (TextUtils.isEmpty(edt_FullName.getText().toString().trim())) {
            edt_FullName.setError(getString(R.string.err_fullname));
            edt_FullName.setFocusable(true);
            return;

        } else if (edt_FullName.getText().toString().trim().length()<3) {
            edt_FullName.setError(getString(R.string.err_fullnamechar));
            edt_FullName.setFocusable(true);
            return;

        }
        else if (TextUtils.isEmpty(edt_EmailAddress.getText().toString().trim())) {
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

        } else if (edt_Password.getText().toString().trim().length()<8) {
            edt_Password.setError(getString(R.string.err_passwordchar));
            edt_Password.setFocusable(true);
            return;

        }

        else {
            if (ConnectivityUtils.isNetworkConnected()) {
                quickbloxSignUp();
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
     * Method used for register new user to quickblox
     */
    private void quickbloxSignUp() {
        progressDialog.show();
        final QbSignUp qbSignUp = new QbSignUp(new QbMyCallBack() {
            @Override
            public void qbSuccess() {
                progressDialog.cancel();
                ((QbActivity) getActivity()).replaceFragment(new QbMessagesFragment());
                ((QbActivity)getActivity()).initvideocall();
            }

            @Override
            public void qbFailure() {
                progressDialog.cancel();
                Toast.makeText(getActivity(), getString(R.string.string_signup_error), Toast.LENGTH_SHORT).show();
            }
        });
        qbSignUp.quickbloxUserSignUp(edt_FullName.getText().toString().trim(), edt_EmailAddress.getText().toString().trim(), edt_Password.getText().toString().trim());

    }

    @Override
    public void onClick(View v) {
        KeyboardUtils.hideKeyboard(edt_Password);
        int i = v.getId();
        if (i == R.id.activity_signup_btn_signup) {
            validationCheck();

        }
    }
}
