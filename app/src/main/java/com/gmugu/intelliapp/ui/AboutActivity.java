package com.gmugu.intelliapp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.gmugu.intelliapp.R;

/**
 * Created by mugu on 17/4/30.
 */

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.dialog_about);
    }
}
