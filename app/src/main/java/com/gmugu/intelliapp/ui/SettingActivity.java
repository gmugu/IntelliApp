package com.gmugu.intelliapp.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.gmugu.intelliapp.R;
import com.gmugu.intelliapp.data.ApiModule;
import com.gmugu.intelliapp.data.ILockApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import haibison.android.lockpattern.LockPatternActivity;
import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = SettingActivity.class.getSimpleName();
    private static final int REQ_OPEN_PATTERN = 1;
    private static final int REQ_CLOSE_PATTERN = 2;
    private static final int REQ_UPDATA_CHECK_PATTERN = 3;
    private static final int REQ_UPDATA_CREATE_PATTERN = 4;
    private static final int REQ_BIND_DEVICE = 5;
    private SwitchPreference passwdPatternPreference;
    private SwitchPreference passwdPatternPreferenceLogin;
    private SwitchPreference passwdPatternPreferenceUplock;
    private SwitchPreference passwdFingerPreference;
    private Preference updataPatternPreference;
    private Preference bindDevicePreference;
    private SharedPreferences defaultSharedPreferences;
    private ILockApi lockApi = ApiModule.provideLockApi();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mContext = this;
        Resources res = getResources();
        passwdPatternPreference = (SwitchPreference) findPreference(res.getString(R.string.key_passwd_pattern));
        passwdPatternPreferenceLogin = (SwitchPreference) findPreference(res.getString(R.string.key_passwd_pattern_login));
        passwdPatternPreferenceUplock = (SwitchPreference) findPreference(res.getString(R.string.key_passwd_pattern_uplock));
        passwdFingerPreference = (SwitchPreference) findPreference(res.getString(R.string.key_passwd_finger));
        updataPatternPreference = findPreference("updataPasswdPattern");
        updataPatternPreference.setOnPreferenceClickListener(this);
        bindDevicePreference = findPreference("bindDevice");
        bindDevicePreference.setOnPreferenceClickListener(this);
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (defaultSharedPreferences.getBoolean(getResources().getString(R.string.key_passwd_pattern), false)) {
            setPatternSubmenuEnable(true);
        } else {
            setPatternSubmenuEnable(false);
        }


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
                Intent intent = new Intent(
                        LockPatternActivity.ACTION_CREATE_PATTERN, null, this,
                        LockPatternActivity.class);
                startActivityForResult(intent, REQ_OPEN_PATTERN);
            } else {
                Intent compare = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                        this, LockPatternActivity.class);
                startActivityForResult(compare, REQ_CLOSE_PATTERN);
            }

        } else if (res.getString(R.string.key_passwd_finger).equals(key)) {

        } else if (res.getString(R.string.key_notification).equals(key)) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQ_OPEN_PATTERN:
                handleOpenPattern(resultCode, data);
                break;
            case REQ_CLOSE_PATTERN:
                handleClosePattern(resultCode, data);
                break;
            case REQ_UPDATA_CHECK_PATTERN:
                handleUpdataCheckPattern(resultCode, data);
                break;
            case REQ_UPDATA_CREATE_PATTERN:
                handleUpdataCreatePattern(resultCode, data);
                break;
            case REQ_BIND_DEVICE:
                handleBindDevice(resultCode, data);
                break;
        }
    }

    private void handleBindDevice(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String result = data.getStringExtra("result");
            int index = -1;
            String name = "";
            String code = "";
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("正在绑定");
            progressDialog.setIndeterminate(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            try {
                index = Integer.parseInt(result.split("=")[1]);
                name = android.os.Build.MODEL;
                name = name.replaceAll(" ", "_");
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                code = wifiManager.getConnectionInfo().getMacAddress();
                lockApi.bindDevice(index, name, code)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ResponseBody>() {
                            @Override
                            public void onCompleted() {
                                progressDialog.cancel();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.cancel();
                            }

                            @Override
                            public void onNext(ResponseBody responseBody) {
                                try {
                                    InputStream in = responseBody.byteStream();
                                    BufferedReader br = new BufferedReader(new InputStreamReader(in, "GB2312"));
                                    String str = br.readLine();
                                    in.close();
                                    new AlertDialog.Builder(mContext)
                                            .setTitle("提示")
                                            .setMessage(str)
                                            .show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                        });

            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.cancel();
                Toast.makeText(this, "绑定失败", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "绑定失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleUpdataCreatePattern(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                break;
            case RESULT_CANCELED:
                break;
        }
    }

    private void handleUpdataCheckPattern(int resultCode, Intent data) {

        switch (resultCode) {
            case RESULT_OK://用户通过验证
                Log.d(TAG, "user passed");
                Intent intent = new Intent(
                        LockPatternActivity.ACTION_CREATE_PATTERN, null, this,
                        LockPatternActivity.class);
                startActivityForResult(intent, REQ_UPDATA_CREATE_PATTERN);
                break;
            case RESULT_CANCELED:// 用户取消
            case LockPatternActivity.RESULT_FAILED://用户多次失败
            case LockPatternActivity.RESULT_FORGOT_PATTERN:// The user forgot the pattern and invoked your recovery Activity.
                break;
        }

    }

    private void handleOpenPattern(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
                setPatternSubmenuEnable(true);
                break;
            case RESULT_CANCELED:
                setPatternSubmenuEnable(false);
                passwdPatternPreference.setChecked(false);
                break;
        }

    }

    private void handleClosePattern(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK://用户通过验证
                Log.d(TAG, "user passed");
                setPatternSubmenuEnable(false);
                break;
            case RESULT_CANCELED:// 用户取消
            case LockPatternActivity.RESULT_FAILED://用户多次失败
            case LockPatternActivity.RESULT_FORGOT_PATTERN:// The user forgot the pattern and invoked your recovery Activity.
                passwdPatternPreference.setChecked(true);
                setPatternSubmenuEnable(true);
                break;
        }

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == updataPatternPreference) {

            Intent compare = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                    this, LockPatternActivity.class);
            startActivityForResult(compare, REQ_UPDATA_CHECK_PATTERN);
        } else if (preference == bindDevicePreference) {
            startActivityForResult(new Intent(this, ScanActivity.class), REQ_BIND_DEVICE);
        }
        return false;
    }

    private void setPatternSubmenuEnable(boolean enable) {
        passwdPatternPreferenceUplock.setEnabled(enable);
        passwdPatternPreferenceLogin.setEnabled(enable);
        updataPatternPreference.setEnabled(enable);
    }
}
