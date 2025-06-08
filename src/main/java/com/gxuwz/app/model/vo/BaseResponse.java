package com.gxuwz.app.model.vo;

import com.gxuwz.app.model.IApiResponse;

/**
 * 响应结果基类
 * @param <T> 响应数据类型
 */
public class BaseResponse<T> implements IApiResponse {
    private String code;
    private String message;
    private T data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public boolean isSuccess() {
        return "200".equals(code);
    }

    @Override
    public String getErrorMessage() {
        return message != null ? message : "未知错误";
    }
}
