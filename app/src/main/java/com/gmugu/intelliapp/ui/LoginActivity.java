package com.gmugu.intelliapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gmugu.intelliapp.R;

import haibison.android.lockpattern.LockPatternActivity;

public class LoginActivity extends Activity implements View.OnClickListener {
    private final static String TAG = LoginActivity.class.getSimpleName();
    private EditText passwdEt;
    private Button loginBn;
    private SharedPreferences preference;

    private static final int REQ_COMPARE_PATTERN = 1;
    private static final int REQ_SET_PASSWD = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        passwdEt = (EditText) findViewById(R.id.passwd_et);
        loginBn = (Button) findViewById(R.id.login_bn);
        loginBn.setOnClickListener(this);
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        String numPasswd = preference.getString(getResources().getString(R.string.key_num_passwd), null);
        if (numPasswd == null) {
            Toast.makeText(this, "您还未设置密码,请先设密码", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(this, UpdatePasswordActivity.class), REQ_SET_PASSWD);
            return;
        }
        if (preference.getBoolean(getResources().getString(R.string.key_passwd_pattern), false)) {
            Intent compare = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                    this, LockPatternActivity.class);
            startActivityForResult(compare, REQ_COMPARE_PATTERN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_SET_PASSWD:
                handleSetPasswd(resultCode, data);
                break;
            case REQ_COMPARE_PATTERN:
                handleComparePattern(resultCode, data);
                break;
        }
    }

    private void handleComparePattern(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK://用户通过验证
                Log.d(TAG, "user passed");
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case RESULT_CANCELED:// 用户取消
                Log.d(TAG, "user cancelled");
                break;
            case LockPatternActivity.RESULT_FAILED://用户多次失败
                Log.d(TAG, "user failed");
                Toast.makeText(this,"请尝试使用密码登录",Toast.LENGTH_SHORT).show();
                break;
            case LockPatternActivity.RESULT_FORGOT_PATTERN:
                // The user forgot the pattern and invoked your recovery Activity.
                Log.d(TAG, "user forgot");
                Toast.makeText(this,"请尝试使用密码登录",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void handleSetPasswd(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                String numPasswd = data.getStringExtra(getResources().getString(R.string.key_num_passwd));
                passwdEt.setText(numPasswd);
                break;
            case RESULT_CANCELED:
                finish();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        String passwd = passwdEt.getText().toString();
        String numPasswd = preference.getString(getResources().getString(R.string.key_num_passwd), null);
        if (numPasswd == null) {
            Toast.makeText(this, "您还未设置密码,请先设密码", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(this, UpdatePasswordActivity.class), 0);
            return;
        }
        if (passwd.equals("")) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwd.length() < 6) {
            Toast.makeText(this, "请输入6位长度以上的密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (numPasswd.equals(passwd)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "密码错误,请重新输入", Toast.LENGTH_SHORT).show();
            passwdEt.setText("");
            return;
        }
    }
}
