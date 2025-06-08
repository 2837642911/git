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

import com.gxuwz.app.R;
import com.gxuwz.app.dao.UserDao;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.model.pojo.User;
import com.gxuwz.app.utils.SessionManager;

public class UpdateFragment extends Fragment {

    private EditText etAccount, etPassword, etRepeatPassword, etCode;
    private Button btnRegister, btnSendCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update, container, false);

        etAccount = view.findViewById(R.id.et_account);
        etPassword = view.findViewById(R.id.et_password);
        etRepeatPassword = view.findViewById(R.id.et_repeat_password);
        etCode = view.findViewById(R.id.et_code);
        btnRegister = view.findViewById(R.id.btn_register);
        btnSendCode = view.findViewById(R.id.btn_send_code);

        btnSendCode.setOnClickListener(v -> {
            // TODO: 发送验证码逻辑
            Toast.makeText(requireContext(), "验证码已发送", Toast.LENGTH_SHORT).show();
        });

        btnRegister.setOnClickListener(v -> {
            String phone = etAccount.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String repeatPassword = etRepeatPassword.getText().toString().trim();
            String code = etCode.getText().toString().trim();

            if (TextUtils.isEmpty(phone) || phone.length() != 11) {
                etAccount.setError("请输入正确的手机号");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("请输入密码");
                return;
            }
            if (!password.equals(repeatPassword)) {
                etRepeatPassword.setError("两次输入的密码不一致");
                return;
            }
            if (TextUtils.isEmpty(code)) {
                etCode.setError("请输入验证码");
                return;
            }

            // 更新数据库中的手机号和密码
            new Thread(() -> {
                int userId = SessionManager.getInstance(requireContext()).getUserId();
                UserDao userDao = AppDatabase.getInstance(requireContext()).userDao();
                User user = userDao.getUserById(userId);
                if (user != null) {
                    user.setPhone(phone);
                    user.setPassword(password);
                    userDao.updateUser(user);
                }
                // 清除Session，强制重新登录
                SessionManager.getInstance(requireContext()).clearLoginState();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "修改成功，请重新登录", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                    startActivity(new android.content.Intent(requireContext(), com.gxuwz.app.activity.LoginActivity.class));
                });
            }).start();
        });

        return view;
    }
}