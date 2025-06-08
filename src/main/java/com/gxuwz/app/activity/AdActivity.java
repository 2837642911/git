package com.gxuwz.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gxuwz.app.R;
import com.gxuwz.app.utils.SessionManager;

public class AdActivity extends AppCompatActivity {

    private ImageView adImage;
    private TextView countdownText;
    private Button skipButton;

    // 本地图片资源数组（放在res/drawable目录）
    private final int[] adResources = {
            R.drawable.ad1,  // 替换为你的本地资源ID
            R.drawable.ad2,
            R.drawable.ad3,
            R.drawable.ad4,
            R.drawable.ad5
    };

    private int currentAdIndex = 0;
    private int countdownSeconds = 5; // 总倒计时5秒
    private final Handler adHandler = new Handler();
    private final Handler countdownHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        // 设置全屏显示（隐藏状态栏）
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        adImage = findViewById(R.id.ad_image);
        countdownText = findViewById(R.id.countdown_text);
        skipButton = findViewById(R.id.skip_button);


        // 检查用户是否已登录
        if (isUserLoggedIn()) {
            // 已登录，直接跳转到MainActivity
            goToMainActivity();
            return; // 终止当前Activity的初始化
        }


        initAdContent();
    }


    private void goToMainActivity() {
        // 停止所有任务
        stopAllHandlers();

        // 跳转到主页面
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private boolean isUserLoggedIn() {
        // 获取userId，如果存在则表示已登录
        int userId = SessionManager.getInstance(this).getUserId();
        return userId != -1; // 根据SessionManager的实现，-1表示未登录
    }

    private void initAdContent() {
        // 初始化显示第一张图片
        showImage(currentAdIndex);

        // 启动轮播（每1秒切换一次）
        startAdRotation();

        // 启动倒计时（5秒后跳转）
        startCountdown();

        // 跳过按钮事件
        skipButton.setOnClickListener(v -> goToLogin());
    }
    private void showImage(int index) {
        // 使用Glide加载本地图片（确保图片占满全屏）
        Glide.with(this)
                .load(adResources[index])
                .dontTransform() // 禁止Glide改变图片尺寸
                .into(adImage);
    }

    private void startAdRotation() {
        adHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 循环播放图片
                currentAdIndex = (currentAdIndex + 1) % adResources.length;
                showImage(currentAdIndex);
                // 每秒切换一次
                adHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void startCountdown() {
        countdownHandler.post(new Runnable() {
            @Override
            public void run() {
                countdownText.setText(String.valueOf(countdownSeconds));

                if (countdownSeconds > 0) {
                    countdownSeconds--;
                    countdownHandler.postDelayed(this, 1000);
                } else {
                    goToLogin();
                }
            }
        });
    }

    private void goToLogin() {
        // 停止所有任务
        stopAllHandlers();

        // 跳转到登录页面
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       stopAllHandlers();
    }
    private void stopAllHandlers() {
        adHandler.removeCallbacksAndMessages(null);
        countdownHandler.removeCallbacksAndMessages(null);
    }
}