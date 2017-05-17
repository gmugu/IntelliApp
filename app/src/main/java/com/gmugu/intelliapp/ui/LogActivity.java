package com.gmugu.intelliapp.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmugu.intelliapp.R;
import com.gmugu.intelliapp.data.ApiModule;
import com.gmugu.intelliapp.data.model.LogBean;
import com.gmugu.intelliapp.data.model.Result;
import com.gmugu.intelliapp.ui.timershaft.TimeShaftChildBean;
import com.gmugu.intelliapp.ui.timershaft.TimeShaftParentBean;
import com.gmugu.intelliapp.ui.timershaft.TimerShaftAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LogActivity extends Activity {
    private final String TAG = getClass().getSimpleName();
    private ExpandableListView elvTimerShaft;
    private TextView elvMsgTv;
    private TimerShaftAdapter adapter;
    private SharedPreferences defaultSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        elvTimerShaft = (ExpandableListView) findViewById(R.id.elv_timer_shaft);
        elvMsgTv = (TextView) findViewById(R.id.elv_msg);

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String lockMac = defaultSharedPreferences.getString(getResources().getString(R.string.key_lock_mac), null);
        if (lockMac == null) {
            Toast.makeText(this, "未绑定门锁,请前往设置中扫描二维码进行绑定", Toast.LENGTH_SHORT).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("正在联网");
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        Observable<Result<List<LogBean>>> observable = ApiModule.provideCloudApi().getLog(lockMac);
        observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Result<List<LogBean>>>() {
                    @Override
                    public void onCompleted() {
                        elvMsgTv.setVisibility(View.GONE);
                        elvTimerShaft.setVisibility(View.VISIBLE);
                        progressDialog.cancel();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        progressDialog.cancel();
                        Toast.makeText(LogActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Result<List<LogBean>> listResult) {
                        List<TimeShaftParentBean> parents = new ArrayList<>();

                        Map<String, List<TimeShaftChildBean>> map = new TreeMap<>(new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return -o1.compareTo(o2);
                            }
                        });

                        List<LogBean> data = listResult.data;
                        for (LogBean logBean : data) {
                            Date time_d = new Date(logBean.getTime());
                            String time = new SimpleDateFormat("MM.dd").format(time_d);
                            if (!map.containsKey(time)) {
                                map.put(time, new ArrayList<TimeShaftChildBean>());
                            }
                            TimeShaftChildBean e = new TimeShaftChildBean();
                            e.setTitle(logBean.getEvent());
                            e.setContent(new SimpleDateFormat("HH:mm").format(time_d));
                            map.get(time).add(e);
                        }
                        for (String key : map.keySet()) {
                            TimeShaftParentBean bean = new TimeShaftParentBean();
                            bean.setTime(key);
                            List<TimeShaftChildBean> list = map.get(key);
                            Collections.sort(
                                    list, new Comparator<TimeShaftChildBean>() {
                                        @Override
                                        public int compare(TimeShaftChildBean o1, TimeShaftChildBean o2) {
                                            return -o1.getContent().compareTo(o2.getContent());
                                        }
                                    });
                            bean.setData(list);
                            parents.add(bean);
                        }

                        adapter = new TimerShaftAdapter(LogActivity.this, parents);
                        elvTimerShaft.setAdapter(adapter);
                        // 遍历所有group,将所有项设置成默认展开
                        for (int i = 0; i < parents.size(); i++) {
                            elvTimerShaft.expandGroup(i);
                        }
                        elvTimerShaft.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

                            @Override
                            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                                Log.d(TAG, "OnGroupClickListener " + groupPosition + " " + id);
                                return true;
                            }
                        });
                        elvTimerShaft.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                            @Override
                            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                                Log.d(TAG, "OnChildClickListener");
                                return false;
                            }
                        });
                    }
                });

    }
}
