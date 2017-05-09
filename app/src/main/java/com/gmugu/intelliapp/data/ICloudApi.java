package com.gmugu.intelliapp.data;

import com.gmugu.intelliapp.data.model.LogBean;
import com.gmugu.intelliapp.data.model.Result;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by mugu on 17/4/28.
 */

public interface ICloudApi {

    @GET("querylog")
    Observable<Result<List<LogBean>>> getLog(@Query("lockMac") String lockMac);

}
