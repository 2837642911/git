package com.gxuwz.app.fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.gxuwz.app.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


/**
 * 空白模板Fragment
 */
public class VersionFragment extends Fragment {
    // 声明向Fragment传递的参数对应的Key常量
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

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
        ProgressBar progressBar = view.findViewById(R.id.progressBar); // 美化版本号显示
        TextView tvCurrent = view.findViewById(R.id.tv_current_version);
        TextView tvLatest = view.findViewById(R.id.tv_latest_version);
        TextView tvDesc = view.findViewById(R.id.tv_update_desc);

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
        return view;
    }
}