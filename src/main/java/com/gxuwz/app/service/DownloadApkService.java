package com.gxuwz.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.gxuwz.app.R;
import com.gxuwz.app.api.UpdateApi;
import com.gxuwz.app.network.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadApkService extends Service {
    public static final String ACTION_START = "com.gxuwz.app.service.DownloadApkService.ACTION_START";
    public static final String EXTRA_URL = "apk_url";
    private static final int NOTIFY_ID = 1001;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("apk_download", "APK下载", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        builder = new NotificationCompat.Builder(this, "apk_download")
                .setContentTitle("正在下载更新包")
                .setSmallIcon(R.drawable.ic_download)
                .setProgress(100, 0, false)
                .setOngoing(true);
        startForeground(NOTIFY_ID, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_START.equals(intent.getAction())) {
            String url = intent.getStringExtra(EXTRA_URL);
            startDownload(url);
        }
        return START_NOT_STICKY;
    }

    private void startDownload(String url) {
        UpdateApi updateApi = RetrofitClient.getUpdateApi();
        Call<ResponseBody> call = updateApi.downloadApk(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("DownloadTest", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DownloadTest", "Content-Length: " + response.body().contentLength());
                    saveApkToDisk(response.body());
                } else {
                    stopSelf();
                    Toast.makeText(getApplicationContext(), "下载失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                stopSelf();
                Toast.makeText(getApplicationContext(), "下载失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveApkToDisk(ResponseBody body) {
        new Thread(() -> {
            File file = null;
            InputStream is = null;
            FileOutputStream os = null;
            long total = 0;
            try {
                file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "test-app.apk");
                is = body.byteStream();
                os = new FileOutputStream(file);
                byte[] buffer = new byte[8192];
                int len;
                long fileSize = body.contentLength();

                while ((len = is.read(buffer)) != -1) {
                    total += len;
                    os.write(buffer, 0, len);
                    int progress = fileSize > 0 ? (int) (total * 100 / fileSize) : 0;
                    updateNotification(progress);
                }
                os.flush();

                Log.d("DownloadTest", "写入文件总字节: " + total);
                updateNotification(100);
                installApk(getApplicationContext(), file);
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getApplicationContext(), "保存文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                try { if (os != null) os.close(); } catch (Exception ignored) {}
                try { if (is != null) is.close(); } catch (Exception ignored) {}
                stopSelf();
            }
        }).start();
    }

    private void updateNotification(int progress) {
        builder.setProgress(100, progress, false)
                .setContentText("下载进度: " + progress + "%");
        notificationManager.notify(NOTIFY_ID, builder.build());
        // 发送进度广播
        Intent intent = new Intent("com.gxuwz.app.DOWNLOAD_PROGRESS");
        intent.putExtra("progress", progress);
        sendBroadcast(intent);
    }

    private void installApk(Context context, File apkFile) {
        if (!apkFile.exists()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
} 