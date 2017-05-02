package com.gmugu.intelliapp.ui.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by mugu on 17/5/1.
 */

public class MyPreference extends Preference {

    private Context mContext;
    private View.OnClickListener onClickListener;

    public MyPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public MyPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyPreference(Context context) {
        this(context, null, 0);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        RelativeLayout rl = new RelativeLayout(getContext());
        rl.setPadding(15, 6, 6, 6);
        TextView tv = new TextView(getContext());
        tv.setText("修改手势密码");
        rl.addView(tv);
        return rl;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if (onClickListener != null) {
            view.setOnClickListener(onClickListener);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
