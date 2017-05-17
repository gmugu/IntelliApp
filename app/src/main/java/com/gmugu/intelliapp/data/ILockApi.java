package com.gmugu.intelliapp.data;

import com.gmugu.intelliapp.data.model.Result;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by mugu on 17/4/28.
 */

public interface ILockApi {

    @GET("bindDevice.cgi")
    Observable<ResponseBody> bindDevice(@Query("index") int index, @Query("name") String name, @Query("code") String code);

    @GET("visitor.img")
    Observable<ResponseBody> getVisitorImg();

    @GET("open.cgi")
    Observable<Result> openLock(@Query("phoneMac") String phoneMac);
}
