package com.gmugu.intelliapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gmugu.intelliapp.R;
import com.gmugu.intelliapp.ui.view.MonitoView;

import haibison.android.lockpattern.LockPatternActivity;

public class VisitorActivity extends Activity implements View.OnClickListener {
    private static final String TAG = VisitorActivity.class.getSimpleName();
    private MonitoView monitoView;
    private View unlockBn;
    private View refreshBn;
    private View ignoreBn;
    private Context mContext;
    private SharedPreferences defaultSharedPreferences;

    private static final int REQ_UNLOCK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);
        mContext = this;
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        unlockBn = findViewById(R.id.unlock_bn);
        refreshBn = findViewById(R.id.refresh_bn);
        ignoreBn = findViewById(R.id.ignore_bn);
        monitoView = (MonitoView) findViewById(R.id.monito_view);

        unlockBn.setOnClickListener(this);
        refreshBn.setOnClickListener(this);
        ignoreBn.setOnClickListener(this);
        monitoView.updataView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        monitoView.recycleBitmap();
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
                MainActivity.unlock(mContext);
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
    public void onClick(View v) {
        switch (v.getId()) {
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
                                    MainActivity.unlock(mContext);
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
                break;
            case R.id.refresh_bn:
                monitoView.updataView();
                break;
            case R.id.ignore_bn:
                finish();
                break;
        }
    }
}
