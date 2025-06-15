package com.gxuwz.app.network;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

import okhttp3.OkHttpClient;

@GlideModule
public class MyGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // 使用与 Retrofit 相同的 OkHttp 客户端
        OkHttpClient client = createOkHttpClient();

        // 注册 OkHttp 为 Glide 的网络层
        registry.replace(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(client));

    }

    private OkHttpClient createOkHttpClient() {
        // 直接从 RetrofitClient 获取 OkHttp 客户端
        // 这样可以确保 Glide 使用与 Retrofit 完全相同的网络配置
        return RetrofitClient.getOkHttpClient();

    }


    @Override
    public boolean isManifestParsingEnabled() {
        return false; // 禁用清单解析以提高性能
    }
}