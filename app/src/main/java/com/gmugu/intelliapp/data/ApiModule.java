package com.gmugu.intelliapp.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mugu on 17/4/28.
 */

public class ApiModule {
    private static ILockApi lockApi;

    public static ILockApi provideLockApi() {
        if (lockApi == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .baseUrl("http://192.168.1.88/")
                    .build();
            lockApi = retrofit.create(ILockApi.class);
        }
        return lockApi;
    }

    private static ICloudApi cloudApi;

    public static ICloudApi provideCloudApi() {
        if (cloudApi == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .baseUrl("http://192.168.23.2:8080/")
                    .build();
            cloudApi = retrofit.create(ICloudApi.class);
        }
        return cloudApi;
    }
}
