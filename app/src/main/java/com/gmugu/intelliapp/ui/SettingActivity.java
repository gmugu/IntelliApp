package com.gmugu.intelliapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gmugu.intelliapp.R;
import com.gmugu.intelliapp.ui.preference.MyPreference;

import haibison.android.lockpattern.LockPatternActivity;

public class SettingActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    private static final String TAG = SettingActivity.class.getSimpleName();
    private static final int REQ_COMPARE_PATTERN = 1;
    private static final int REQ_CREATE_PATTERN = 2;
    private SwitchPreference passwdPatternPreference;
    private SwitchPreference passwdFingerPreference;
    private MyPreference myPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Resources res = getResources();
        passwdPatternPreference = (SwitchPreference) findPreference(res.getString(R.string.key_passwd_pattern));
        passwdFingerPreference = (SwitchPreference) findPreference(res.getString(R.string.key_passwd_finger));
        myPreference = (MyPreference) findPreference("updataPasswdPattern");
        myPreference.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, key);
        Resources res = getResources();
        if (res.getString(R.string.key_passwd_pattern).equals(key)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            if (value) {
                Intent compare = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                        this, LockPatternActivity.class);
                startActivityForResult(compare, REQ_COMPARE_PATTERN);
            }

        } else if (res.getString(R.string.key_passwd_finger).equals(key)) {

        } else if (res.getString(R.string.key_notification).equals(key)) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQ_COMPARE_PATTERN:
                handleComparePattern(resultCode, data);
                break;
            case REQ_CREATE_PATTERN:
                handleCreatePattern(resultCode, data);
                break;
        }
    }

    private void handleCreatePattern(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
                myPreference.setEnabled(true);
                break;
            case RESULT_CANCELED:
                passwdPatternPreference.setChecked(false);
                break;
        }

    }

    private void handleComparePattern(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK://用户通过验证
                Log.d(TAG, "user passed");
                Intent intent = new Intent(
                        LockPatternActivity.ACTION_CREATE_PATTERN, null, this,
                        LockPatternActivity.class);
                startActivityForResult(intent, REQ_CREATE_PATTERN);
                break;
            case RESULT_CANCELED:// 用户取消
                Log.d(TAG, "user cancelled");
                passwdPatternPreference.setChecked(false);
                break;
            case LockPatternActivity.RESULT_FAILED://用户多次失败
                Log.d(TAG, "user failed");
                passwdPatternPreference.setChecked(false);
                break;
            case LockPatternActivity.RESULT_FORGOT_PATTERN:
                // The user forgot the pattern and invoked your recovery Activity.
                Log.d(TAG, "user forgot");
                passwdPatternPreference.setChecked(false);
                break;
        }

        // 在任何情况下，EXTRA_RETRY_COUNT都代表着用户尝试的图案的次数
        int retryCount = data.getIntExtra(
                LockPatternActivity.EXTRA_RETRY_COUNT, 0);
        Log.i(TAG, "用户尝试了" + retryCount + "次数");
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this,"Sgs",Toast.LENGTH_SHORT).show();
    }
}
