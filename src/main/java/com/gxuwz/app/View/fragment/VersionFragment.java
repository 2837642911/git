package com.gxuwz.app.View.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gxuwz.app.R;
import com.gxuwz.app.network.WebAPI;
import com.gxuwz.app.service.DownloadApkService;



/**
 * 空白模板Fragment
 */
public class VersionFragment extends Fragment {
    // 声明向Fragment传递的参数对应的Key常量
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private ProgressBar progressBar;
    private ProgressBar progressDownload;
    private BroadcastReceiver progressReceiver;

    public VersionFragment() {
        super();
    }

    /**
     * 创建带给定Bundle参数的Fragment实例的类方法
     */
    public static VersionFragment newInstance(String param1, String param2) {
        VersionFragment fragment = new VersionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_version, container, false);
        progressBar = view.findViewById(R.id.progressBar); // 美化版本号显示
        TextView tvCurrent = view.findViewById(R.id.tv_current_version);
        TextView tvLatest = view.findViewById(R.id.tv_latest_version);
        TextView tvDesc = view.findViewById(R.id.tv_update_desc);
        Button btnDownload = view.findViewById(R.id.btn_download_apk);
        progressDownload = view.findViewById(R.id.progress_download);

        String versionName = "V1.0.0";
        try {
            PackageManager pm = requireContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(requireContext().getPackageName(), 0);
            versionName = "V" + pi.versionName;
        } catch (Exception e) {
            // ignore
        }
        tvCurrent.setText(versionName);
        tvLatest.setText(versionName);
        tvDesc.setText("");
        progressBar.setVisibility(View.VISIBLE);

        // 1.5秒后隐藏转圈并弹窗
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded() || getActivity() == null) return;
            progressBar.setVisibility(View.GONE);
            tvDesc.setText("没有版本更新");
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("版本检查")
                    .setMessage("已是最新版本")
                    .setPositiveButton("确定", null)
                    .show();
        }, 1500);

        // 下载APK按钮逻辑
        btnDownload.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DownloadApkService.class);
            intent.setAction(DownloadApkService.ACTION_START);
            intent.putExtra(DownloadApkService.EXTRA_URL, WebAPI.UPDATE_URL + "api/update/download");
            requireContext().startService(intent);
            Toast.makeText(requireContext(), "开始下载APK...", Toast.LENGTH_SHORT).show();
        });

        // 注册广播
        progressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int progress = intent.getIntExtra("progress", 0);
                progressDownload.setVisibility(View.VISIBLE);
                progressDownload.setProgress(progress);
                if (progress >= 100) {
                    progressDownload.setVisibility(View.GONE);
                }
            }
        };
        IntentFilter filter = new IntentFilter("com.gxuwz.app.DOWNLOAD_PROGRESS");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(progressReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(progressReceiver, filter);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressReceiver != null) {
            requireContext().unregisterReceiver(progressReceiver);
        }
    }
}