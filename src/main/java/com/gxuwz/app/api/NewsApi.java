package com.gxuwz.app.api;

import com.gxuwz.app.model.network.NewsDetailResponse;
import com.gxuwz.app.model.network.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApi {
    @GET("toutiao/index")
    Call<NewsResponse> getNewsList(
        @Query("key") String key,
        @Query("type") String type,
        @Query("page") int page,
        @Query("page_size") int pageSize,
        @Query("is_filter") int isFilter
    );

    @GET("toutiao/content")
    Call<NewsDetailResponse> getNewsDetail(
        @Query("key") String key,
        @Query("uniquekey") String uniquekey
    );
} 