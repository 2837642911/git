package com.gxuwz.app.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.gxuwz.app.R;
import com.gxuwz.app.dao.UserDao;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.model.pojo.User;
import com.gxuwz.app.utils.SessionManager;

public class SettingFragment extends Fragment {

    private EditText etUserName;
    private Button btnSave;
    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        etUserName = view.findViewById(R.id.et_user_name);
        btnSave = view.findViewById(R.id.btn_save);

        // 获取当前用户
        user = getCurrentUser();
        if (user != null) {
            etUserName.setText(user.getUserName());
        }

        btnSave.setOnClickListener(v -> {
            String newUserName = etUserName.getText().toString().trim();
            if (TextUtils.isEmpty(newUserName)) {
                Toast.makeText(requireContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (user != null) {
                user.setUserName(newUserName);
                // 更新数据库
                new Thread(() -> {
                    UserDao userDao = AppDatabase.getInstance(requireContext()).userDao();
                    userDao.updateUser(user);
                    // 更新Session
                    SessionManager.getInstance(requireContext()).saveLoginState(user);
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
                        // 跳转回用户界面
                        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
                        if (viewPager != null) {
                            viewPager.setCurrentItem(FragmentConstants.MeFragment, true);
                        }
                    });
                }).start();
            }
        });

        // 手机号和密码修改跳转
        View updateLayout = view.findViewById(R.id.layout_update);
        updateLayout.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
            if (viewPager != null) {
                viewPager.setCurrentItem(FragmentConstants.UpdateFragment, true);
            }
        });

        return view;
    }

    private User getCurrentUser() {
        int userId = SessionManager.getInstance(requireContext()).getUserId();
        UserDao userDao = AppDatabase.getInstance(requireContext()).userDao();
        return userDao.getUserById(userId);
    }
}