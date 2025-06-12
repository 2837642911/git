package com.gxuwz.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

public class CategoryManager {
    private static final String PREFS_NAME = "news_categories";
    private static final String KEY_TYPES = "types";
    private static final String KEY_TITLES = "titles";
    // 默认分类
    private static final String[] DEFAULT_TYPES = {"top", "guonei", "guoji", "junshi", "keji"};
    private static final String[] DEFAULT_TITLES = {"推荐", "国内", "国际", "军事", "科技"};

    public static void saveCategories(Context context, List<String> types, List<String> titles) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_TYPES, TextUtils.join(",", types))
                .putString(KEY_TITLES, TextUtils.join(",", titles))
                .apply();
    }

    public static List<String> getTypes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String str = prefs.getString(KEY_TYPES, null);
        if (str == null) return Arrays.asList(DEFAULT_TYPES);
        return Arrays.asList(str.split(","));
    }

    public static List<String> getTitles(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String str = prefs.getString(KEY_TITLES, null);
        if (str == null) return Arrays.asList(DEFAULT_TITLES);
        return Arrays.asList(str.split(","));
    }

    public static List<String> getAllTypes() {
        return Arrays.asList(DEFAULT_TYPES);
    }
    public static List<String> getAllTitles() {
        return Arrays.asList(DEFAULT_TITLES);
    }
}