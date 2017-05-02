package com.gmugu.intelliapp.data;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mugu on 17/4/28.
 */

public class ApiModule {
    private static ILockApi lockApi;
    public static ILockApi provideLockApi(){
        if (lockApi==null){
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//新的配置
                    .baseUrl("http://192.168.1.88/")
                    .build();
            lockApi = retrofit.create(ILockApi.class);
        }
        return lockApi;
    }

    private static ICloudApi cloudApi;
    public static ICloudApi provideCloudApi(){
        if (cloudApi==null){
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//新的配置
                    .baseUrl("192.168.1.88/")
                    .build();
            cloudApi = retrofit.create(ICloudApi.class);
        }
        return cloudApi;
    }
}
