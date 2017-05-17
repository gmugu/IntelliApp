package com.gmugu.intelliapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gmugu.intelliapp.R;
import com.gmugu.intelliapp.data.ApiModule;
import com.gmugu.intelliapp.data.model.Result;

import java.util.List;

import haibison.android.lockpattern.LockPatternActivity;
import haibison.android.lockpattern.utils.AlpSettings;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks, View.OnClickListener {
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQ_UNLOCK = 1;
    private View unlockBn;
    private View visitorBn;
    private View logBn;
    private View settingBn;
    private Context mContext;
    private SharedPreferences defaultSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        initView();
        AlpSettings.Security.setAutoSavePattern(this, true);
    }

    private void initView() {
        unlockBn = findViewById(R.id.unlock_bn);
        visitorBn = findViewById(R.id.visitor_bn);
        logBn = findViewById(R.id.log_bn);
        settingBn = findViewById(R.id.setting_bn);
        unlockBn.setOnClickListener(this);
        visitorBn.setOnClickListener(this);
        logBn.setOnClickListener(this);
        settingBn.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        requestCodeQRCodePermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_UNLOCK:
                handleLockPattern(resultCode, data);
                break;
        }
    }


    private void handleLockPattern(int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                //用户通过验证
                Log.d(TAG, "user passed");
                unlock(mContext);
                break;
            case RESULT_CANCELED:
                // 用户取消
                Log.d(TAG, "user cancelled");
                break;
            case LockPatternActivity.RESULT_FAILED:
                //用户多次失败
                Log.d(TAG, "user failed");
                break;
            case LockPatternActivity.RESULT_FORGOT_PATTERN:
                // The user forgot the pattern and invoked your recovery Activity.
                Log.d(TAG, "user forgot");
                break;
        }

        // 在任何情况下，EXTRA_RETRY_COUNT都代表着用户尝试的图案的次数
        int retryCount = data.getIntExtra(
                LockPatternActivity.EXTRA_RETRY_COUNT, 0);
        Log.i(TAG, "用户尝试了" + retryCount + "次数");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.unlock_bn:
                boolean isUseFinger = defaultSharedPreferences.getBoolean(getResources().getString(R.string.key_is_passwd_finger), false);
                boolean isUsePattern = defaultSharedPreferences.getBoolean(getResources().getString(R.string.key_is_passwd_pattern), false)
                        && defaultSharedPreferences.getBoolean(getResources().getString(R.string.key_is_passwd_pattern_uplock), false);
                if (isUseFinger) {
// TODO: 17/5/17 指纹
                } else if (isUsePattern) {
                    Intent compare = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                            this, LockPatternActivity.class);
                    startActivityForResult(compare, REQ_UNLOCK);
                } else {
                    final EditText editText = new EditText(this);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                    new AlertDialog.Builder(this)
                            .setTitle("请输入密码")
                            .setView(editText)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String passwd = editText.getText().toString();
                                    if (passwd.equals("")) {
                                        Toast.makeText(mContext, "密码不能为空", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if (passwd.length() < 6) {
                                        Toast.makeText(mContext, "密码长度必须大于6", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    String numPsaawd = defaultSharedPreferences.getString(getResources().getString(R.string.key_num_passwd), null);
                                    if (!passwd.equals(numPsaawd)) {
                                        Toast.makeText(mContext, "密码错误", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    unlock(mContext);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }


                break;
            case R.id.visitor_bn:
                startActivity(new Intent(this, VisitorActivity.class));
                break;
            case R.id.log_bn:
                startActivity(new Intent(this, LogActivity.class));
                break;
            case R.id.setting_bn:
                startActivity(new Intent(this, SettingActivity.class));
                break;
        }
    }

    public static void unlock(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String code = wifiManager.getConnectionInfo().getMacAddress();
        if (code == null) {
            Toast.makeText(context, "请打开WIFI", Toast.LENGTH_SHORT).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("正在解锁");
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        ApiModule.provideLockApi().unlock(code)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Result>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.cancel();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(context, "未知错误:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                    }

                    @Override
                    public void onNext(Result result) {
                        try {
                            if (result.code != 0) {
                                throw new Exception(result.msg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "解锁失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }

                });

    }
}
