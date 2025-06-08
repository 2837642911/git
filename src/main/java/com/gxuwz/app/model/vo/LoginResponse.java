package com.gxuwz.app.model.vo;


import com.gxuwz.app.model.pojo.User;

/**
 * 登录响应对象
 */
public class LoginResponse extends BaseResponse<User> {
    private String token;//令牌
}
