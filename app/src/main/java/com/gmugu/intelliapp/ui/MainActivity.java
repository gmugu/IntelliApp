package com.gmugu.intelliapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gmugu.intelliapp.R;

import java.util.List;

import haibison.android.lockpattern.LockPatternActivity;
import haibison.android.lockpattern.utils.AlpSettings;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks, View.OnClickListener {
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQ_UNLOCK = 1;
    private View unlockBn;
    private View visitorBn;
    private View logBn;
    private View settingBn;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
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
                Intent compare = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                        this, LockPatternActivity.class);
                startActivityForResult(compare, REQ_UNLOCK);

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
}
