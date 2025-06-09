package com.gxuwz.app.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface UpdateApi {
    @Streaming
    @GET
    Call<ResponseBody> downloadApk(@Url String fileUrl);
} 