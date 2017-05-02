package com.gmugu.intelliapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gmugu.intelliapp.R;
import com.gmugu.intelliapp.data.ApiModule;
import com.gmugu.intelliapp.data.ILockApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import haibison.android.lockpattern.LockPatternActivity;
import haibison.android.lockpattern.utils.AlpSettings;
import okhttp3.ResponseBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks, View.OnClickListener {
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQ_SCAN_REQUEST = 1;
    private static final int REQ_COMPARE_PATTERN = 4;
    private static final int REQ_CREATE_PATTERN = 5;
    private View unlockBn;
    private View scanBn;
    private View visitorBn;
    private View logBn;
    private View settingBn;
    private Context mContext;

    private ILockApi lockApi = ApiModule.provideLockApi();

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
        scanBn = findViewById(R.id.scan_bn);
        visitorBn = findViewById(R.id.visitor_bn);
        logBn = findViewById(R.id.log_bn);
        settingBn = findViewById(R.id.setting_bn);
        unlockBn.setOnClickListener(this);
        scanBn.setOnClickListener(this);
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
            case REQ_SCAN_REQUEST:
                handleScan(resultCode, data);
                break;
            case REQ_CREATE_PATTERN:
                if (resultCode == RESULT_OK) {
                    char[] pattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
                    StringBuffer buffer = new StringBuffer();
                    for(char c:pattern){
                        buffer.append(c);
                    }
                    Log.i(TAG, "result=>"+buffer.toString());
                    Toast.makeText(this, "消息摘要："+buffer, Toast.LENGTH_SHORT).show();
                    //test:101b2a675e9fb9546336d5b9ef70418b594184f4
                }
                break;
            case REQ_COMPARE_PATTERN:
                handleLockPattern(resultCode, data);
                break;
        }
    }


    private void handleScan(int resultCode, Intent data) {
        if (resultCode == ScanActivity.SUCCESS) {
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
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<ResponseBody>() {
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
//                char[] savedPattern = testChars.toCharArray();
                Intent compare = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,
                        this, LockPatternActivity.class);
//                compare.putExtra(LockPatternActivity.EXTRA_PATTERN, savedPattern);
                startActivityForResult(compare, REQ_COMPARE_PATTERN);

                break;
            case R.id.scan_bn:
                startActivityForResult(new Intent(this, ScanActivity.class), REQ_SCAN_REQUEST);
                break;
            case R.id.visitor_bn:
                Intent intent = new Intent(
                        LockPatternActivity.ACTION_CREATE_PATTERN, null, this,
                        LockPatternActivity.class);
                startActivityForResult(intent, REQ_CREATE_PATTERN);
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
