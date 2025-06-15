package com.gxuwz.app.network;

import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.api.UpdateApi;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String NEWS_URL = WebAPI.NEWS_URL;
    private static Retrofit retrofit = null;
    private static NewsApi newsApi = null;
    private static OkHttpClient okHttpClient = null;

    // 获取共享的 OkHttp 客户端实例
    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(
                            10,
                            2, TimeUnit.MINUTES
                    ))
                    .build();

        }
        return okHttpClient;
    }

    public static NewsApi getNewsApi() {
        if (newsApi == null) {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(NEWS_URL)
                        .client(getOkHttpClient()) // 使用共享的 OkHttp 客户端
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            newsApi = retrofit.create(NewsApi.class);
        }
        return newsApi;
    }

    public static UpdateApi getUpdateApi() {
        return new Retrofit.Builder()
                .baseUrl(WebAPI.UPDATE_URL)
                .client(getOkHttpClient()) // 使用相同的 OkHttp 客户端
                .build()
                .create(UpdateApi.class);
    }
}