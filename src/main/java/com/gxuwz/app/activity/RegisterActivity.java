package com.gxuwz.app.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gxuwz.app.R;
import com.gxuwz.app.dao.UserDao;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.model.pojo.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etAccount, etPassword, etRepeatPassword, etCode;
    private Button btnSendCode, btnRegister;
    private CountDownTimer countDownTimer;
    private ExecutorService executorService; // 线程池

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        etRepeatPassword = findViewById(R.id.et_repeat_password);
        etCode = findViewById(R.id.et_code);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnRegister = findViewById(R.id.btn_register);

        // 初始化线程池
        executorService = Executors.newSingleThreadExecutor();

        btnSendCode.setOnClickListener(v -> sendCode());
        btnRegister.setOnClickListener(v -> register());

        TextView tvToLogin = findViewById(R.id.tv_to_login);
        tvToLogin.setOnClickListener(v -> {

            finish();
        });
    }

    private void sendCode() {
        String phone = etAccount.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || phone.length() != 11) {
            etAccount.setError("请输入正确的手机号");
            return;
        }
        // TODO: 调用API发送验证码
        Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();
        startCountDown();
    }

    private void startCountDown() {
        btnSendCode.setEnabled(false);
        countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                btnSendCode.setText(millisUntilFinished / 1000 + "s");
            }
            public void onFinish() {
                btnSendCode.setText("发送验证码");
                btnSendCode.setEnabled(true);
            }
        }.start();
    }

    private void register() {
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

        // 在后台线程执行数据库操作
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(RegisterActivity.this);
            UserDao userDao = db.userDao();

            // 检查用户是否已存在
            User exist = userDao.getUserByPhone(phone);

            if (exist != null) {
                // 切换回主线程显示Toast
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "手机号已注册", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // 插入新用户
            User user = new User(phone, password);
            user.setUserName("王小明");
            long result = userDao.insertUser(user);

            // 回到主线程处理结果
            runOnUiThread(() -> {
                if (result != -1) {
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}