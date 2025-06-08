package com.gxuwz.app.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.gxuwz.app.model.IApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 统一处理网络请求响应
 */
public class ResponseHandler {
    private static final String TAG = "ResponseHandler";

    /**
     * 处理响应结果（支持所有实现了IApiResponse的响应类）
     * @param context 上下文
     * @param call 网络请求
     * @param callback 回调接口
     * @param <T> 响应数据类型
     */
    public static <T extends IApiResponse> void handleResponse(Context context, Call<T> call, ResponseCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                Log.d(TAG, "onResponse: Response code = " + response.code());
                if (!response.isSuccessful() || response.body() == null) {
                    handleFailure(context, "请求失败: " + response.code(), callback);
                    return;
                }
                T apiResponse = response.body();
                Log.d(TAG, "onResponse: isSuccess = " + apiResponse.isSuccess());
                Log.d(TAG, "onResponse: error message = " + apiResponse.getErrorMessage());
                if (apiResponse.isSuccess()) {
                    handleSuccess(apiResponse, callback);
                } else {
                    handleFailure(context, apiResponse.getErrorMessage(), callback);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                handleFailure(context, "网络错误: " + t.getMessage(), callback);
            }
        });
    }

    private static <T> void handleSuccess(T response, ResponseCallback<T> callback) {
        if (callback != null) {
            callback.onSuccess(response);
        }
    }

    private static void handleFailure(Context context, String errorMsg, ResponseCallback<?> callback) {
        Log.e(TAG, "onFailure: " + errorMsg);
        showToast(context, errorMsg);
        if (callback != null) {
            callback.onError(errorMsg);
        }
    }

    private static void showToast(Context context, String msg) {
        if (context != null && msg != null) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
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