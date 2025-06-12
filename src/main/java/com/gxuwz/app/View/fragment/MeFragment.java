package com.gxuwz.app.View.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.gxuwz.app.R;
import com.gxuwz.app.View.activity.LoginActivity;
import com.gxuwz.app.View.activity.MainActivity;


/**
 * 空白模板Fragment
 */
public class MeFragment extends Fragment {
    // 声明向Fragment传递的参数对应的Key常量
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public MeFragment() {
        super();
    }

    /**
     * 创建带给定Bundle参数的Fragment实例的类方法
     */
    public static MeFragment newInstance(String param1, String param2) {
        MeFragment fragment = new MeFragment();
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
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        setupVersionClick(view);
        setupLogoutClick(view);
        setupRecordClick(view);
        setupProfileClick(view);
        return view;
    }

    private void setupVersionClick(View view) {
        View versionLayout = view.findViewById(R.id.layout_version);
        versionLayout.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).replaceFragment(FragmentConstants.VersionFragment, true);
            }
        });
    }

    private void setupLogoutClick(View view) {
        View logoutLayout = view.findViewById(R.id.layout_loginout);
        logoutLayout.setOnClickListener(v -> {
            com.gxuwz.app.utils.SessionManager.getInstance(requireContext()).clearLoginState();
            requireActivity().finish();
            startActivity(new android.content.Intent(requireContext(), LoginActivity.class));
        });
    }

    private void setupProfileClick(View view) {
        View profileLayout = view.findViewById(R.id.layout_profile);
        if (profileLayout != null) {
            profileLayout.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).replaceFragment(FragmentConstants.SettingFragment, true);
                }
            });
        }
    }

    private void setupRecordClick(View view) {
        View recordLayout = view.findViewById(R.id.layout_record);
        if (recordLayout != null) {
            recordLayout.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).replaceFragment(FragmentConstants.ProfileRecordFragment, true);
                }
            });
        }
    }

}