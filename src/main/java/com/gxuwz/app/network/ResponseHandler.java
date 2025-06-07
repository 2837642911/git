package com.gxuwz.app.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.gxuwz.app.model.vo.BaseResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 统一处理网络请求响应
 */
public class ResponseHandler {
    private static final String TAG = "ResponseHandler";
    
    /**
     * 处理响应结果
     * @param context 上下文
     * @param call 网络请求
     * @param callback 回调接口
     * @param <T> 响应数据类型
     */
    public static <T extends BaseResponse<?>> void handleResponse(Context context, Call<T> call, ResponseCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                Log.d(TAG, "onResponse: Response code = " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    T baseResponse = response.body();
                    Log.d(TAG, "onResponse: Response body = " + baseResponse);
                    Log.d(TAG, "onResponse: isSuccess = " + baseResponse.isSuccess());
                    Log.d(TAG, "onResponse: error message = " + baseResponse.getErrorMessage());
                    
                    if (baseResponse.isSuccess()) {
                        callback.onSuccess(baseResponse);
                    } else {
                        String errorMsg = baseResponse.getErrorMessage();
                        Log.e(TAG, "onResponse: API error - " + errorMsg);
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                        callback.onError(errorMsg);
                    }
                } else {
                    String errorMsg = "请求失败: " + response.code();
                    Log.e(TAG, "onResponse: " + errorMsg);
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                String errorMsg = "网络错误: " + t.getMessage();
                Log.e(TAG, "onFailure: " + errorMsg, t);
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * 响应回调接口
     * @param <T> 响应数据类型
     */
    public interface ResponseCallback<T> {
        /**
         * 请求成功回调
         * @param response 响应数据
         */
        void onSuccess(T response);

        /**
         * 请求失败回调
         * @param errorMsg 错误信息
         */
        void onError(String errorMsg);
    }
} 