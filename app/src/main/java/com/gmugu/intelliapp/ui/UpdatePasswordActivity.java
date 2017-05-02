package com.gmugu.intelliapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmugu.intelliapp.R;

/**
 * Created by mugu on 17/5/1.
 */

public class UpdatePasswordActivity extends Activity implements View.OnClickListener {

    private EditText oldPasswdEt;
    private TextView oldPasswdTv;
    private EditText newPasswdEt;
    private EditText newPasswdEt2;
    private Button sureBn;
    private SharedPreferences preferences;
    private String numPsaawd;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updata_password);
        res = getResources();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        numPsaawd = preferences.getString(res.getString(R.string.key_num_passwd), null);

        oldPasswdEt = (EditText) findViewById(R.id.old_password_et);
        oldPasswdTv = (TextView) findViewById(R.id.old_password_tv);
        newPasswdEt = (EditText) findViewById(R.id.new_password_et);
        newPasswdEt2 = (EditText) findViewById(R.id.new_password2_et);
        sureBn = (Button) findViewById(R.id.sure_bn);
        sureBn.setOnClickListener(this);

        if (numPsaawd == null) {
            oldPasswdEt.setVisibility(View.GONE);
            oldPasswdTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        String newPasswd = newPasswdEt.getText().toString();
        String newPasswd2 = newPasswdEt2.getText().toString();
        String oldpasswd;
        if (numPsaawd != null) {
            oldpasswd = oldPasswdEt.getText().toString();
            if (oldpasswd.equals("")) {
                Toast.makeText(this, "请输入原密码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!oldpasswd.equals(numPsaawd)) {
                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (newPasswd.equals("")) {
            Toast.makeText(this, "新密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPasswd.length() < 6) {
            Toast.makeText(this, "新密码长度必须大于6", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPasswd.equals(newPasswd2)) {
            Toast.makeText(this, "两次输入的密码不一样", Toast.LENGTH_SHORT).show();
            return;
        }
        numPsaawd = newPasswd;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(res.getString(R.string.key_num_passwd), numPsaawd);
        editor.commit();
        Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
        Intent data = new Intent();
        data.putExtra(getResources().getString(R.string.key_num_passwd), numPsaawd);
        setResult(RESULT_OK, data);
        finish();
    }
}
