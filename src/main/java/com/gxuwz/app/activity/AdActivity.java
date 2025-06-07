package com.gxuwz.app.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.gxuwz.app.R;

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
        adHandler.removeCallbacksAndMessages(null);
        countdownHandler.removeCallbacksAndMessages(null);

        // 跳转到登录页面
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adHandler.removeCallbacksAndMessages(null);
        countdownHandler.removeCallbacksAndMessages(null);
    }
}