package com.gxuwz.app.network;

import com.gxuwz.app.api.NewsApi;
import com.gxuwz.app.api.UpdateApi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String NEWS_URL = WebAPI.NEWS_URL;
    private static Retrofit retrofit = null;
    private static NewsApi newsApi = null;

    public static NewsApi getNewsApi() {
        if (newsApi == null) {
            if (retrofit == null) {
                // 配置OkHttpClient
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)  // 连接超时
                    .readTimeout(10, TimeUnit.SECONDS)     // 读取超时
                    .writeTimeout(10, TimeUnit.SECONDS)    // 写入超时
                    .build();

                retrofit = new Retrofit.Builder()
                    .baseUrl(NEWS_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            }
            newsApi = retrofit.create(NewsApi.class);
        }
        return newsApi;
    }

    public static UpdateApi getUpdateApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WebAPI.UPDATE_URL)
                .client(new OkHttpClient.Builder().build())
                .build();
        return retrofit.create(UpdateApi.class);
    }
}