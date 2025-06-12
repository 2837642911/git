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

    // 天猫商品页面链接数组
    private final String[] adLinks = {
        "https://detail.tmall.com/item.htm?id=899924971005&spm=608.29877528.5652494050.3.7a244d19yWDkl4&fromChannel=bybtChannel&fpChannel=101&fpChannelSig=3f91de8bbbdb27f39270d954d92b124c394e16b4&skuId=5946083950459&ltk2=1749395358729qxurp3wszagvmik6og1tbj",
        "https://detail.tmall.com/item.htm?id=849444808803&spm=608.29877528.5652494050.4.7a244d19yWDkl4&fromChannel=bybtChannel&fpChannel=101&fpChannelSig=c4895e099999c7aabfcf975afcd49781c21a0398&skuId=5722764980929&ltk2=1749395718002b64uyp9osjqdsokvdex01f",
        "https://detail.tmall.com/item.htm?id=762937182911&spm=608.29877528.5652494050.5.7a244d19yWDkl4&fromChannel=bybtChannel&fpChannel=101&fpChannelSig=43608f6c82b85e341d4f22b86946aaa72b7c48a2&skuId=5261102176401&ltk2=1749395723563l0rx9hhplmebwumznd9fd4",
        "https://detail.tmall.com/item.htm?id=864550291039&spm=608.29877528.5652494050.16.30a94d19PN7P4m&fromChannel=bybtChannel&fpChannel=101&fpChannelSig=36308c7c9e109ca6e68f9c79db1cd8626c4ffce6&skuId=5707168836218&ltk2=1749395739771c8r0nvm2juulnxqgsnhcl",
        "https://detail.tmall.com/item.htm?id=753781577673&spm=608.29877528.5652494050.14.30a94d19PN7P4m&fromChannel=bybtChannel&fpChannel=101&fpChannelSig=f2953410f87309069d8393da7600510d7eca82ae&skuId=5862500654218&ltk2=1749395749090i4v6n74ykhp8ie7w56d2nv"
    };

    private int currentAdIndex = 0;
    private int countdownSeconds = 5; // 总倒计时5秒
    private final Handler adHandler = new Handler();
    private final Handler countdownHandler = new Handler();
    private boolean isPausedByAdClick = false;

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

        // 点击广告图片时用浏览器打开商品页面
        adImage.setOnClickListener(v -> {
            stopAllHandlers();
            isPausedByAdClick = true;
            openInBrowser(adLinks[currentAdIndex]);
        });
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

    private void openInBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "无法打开浏览器", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPausedByAdClick) {
            isPausedByAdClick = false;
            startCountdown();
            startAdRotation();
        }
    }
}