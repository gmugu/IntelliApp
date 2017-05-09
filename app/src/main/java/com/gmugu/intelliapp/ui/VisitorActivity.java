package com.gmugu.intelliapp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.gmugu.intelliapp.R;
import com.gmugu.intelliapp.ui.view.MonitoView;

public class VisitorActivity extends Activity implements View.OnClickListener {
    private MonitoView monitoView;
    private View unlockBn;
    private View refreshBn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);
        unlockBn = findViewById(R.id.unlock_bn);
        refreshBn = findViewById(R.id.refresh_bn);
        monitoView = (MonitoView) findViewById(R.id.monito_view);

        unlockBn.setOnClickListener(this);
        refreshBn.setOnClickListener(this);
        monitoView.updataView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        monitoView.recycleBitmap();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.unlock_bn:

                break;
            case R.id.refresh_bn:
                monitoView.updataView();
                break;
        }
    }
}
