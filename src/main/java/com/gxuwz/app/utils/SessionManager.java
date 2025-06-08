package com.gxuwz.app.utils;



import android.content.Context;
import android.content.SharedPreferences;

import com.gxuwz.app.model.pojo.User;

public class SessionManager {
    private static final String PREF_NAME = "AppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_USERNAME = "userName";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    private static SessionManager sessionManagerInstance;


    // 构造函数
    public SessionManager(Context context) {
        this.context =  context.getApplicationContext();
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    // 静态方法获取单例实例（双重检查锁定）
    public static SessionManager getInstance(Context context) {
        if (sessionManagerInstance == null) {
            synchronized (SessionManager.class) {
                if (sessionManagerInstance == null) {
                    sessionManagerInstance = new SessionManager(context);
                }
            }
        }
        return sessionManagerInstance;
    }


    // 保存登录状态
    public void saveLoginState(int userId, String phone, String userName) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_USERNAME, userName);
        editor.apply();
    }
    // 在 SessionManager 类中添加这个方法
    public void saveLoginState(User user) {
        if (user != null) {
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putInt(KEY_USER_ID, user.getUserId());
            editor.putString(KEY_PHONE, user.getPhone());
            editor.putString(KEY_USERNAME, user.getUserName());
            editor.apply();
        }
    }

    // 检查用户是否已登录
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // 获取用户ID
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    // 获取手机号
    public String getPhone() {
        return pref.getString(KEY_PHONE, null);
    }

    // 获取用户名
    public String getUserName() {
        return pref.getString(KEY_USERNAME, null);
    }

    // 清除登录状态
    public void clearLoginState() {
        editor.clear();
        editor.apply();
    }
}