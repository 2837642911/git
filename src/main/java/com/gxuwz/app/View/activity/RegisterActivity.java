package com.gxuwz.app.View.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gxuwz.app.R;
import com.gxuwz.app.model.pojo.User;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.dao.UserDao;


public class RegisterActivity extends AppCompatActivity {

    private EditText etAccount, etPassword, etRepeatPassword, etCode;
    private Button btnSendCode, btnRegister;
    private CountDownTimer countDownTimer;
    private UserDao userDao;

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

        userDao = AppDatabase.getInstance(this).userDao();

        btnSendCode.setOnClickListener(v -> sendCode());
        btnRegister.setOnClickListener(v -> register());

        TextView tvToLogin = findViewById(R.id.tv_to_login);
        tvToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
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

        // Service层操作
        User exist = userDao.getUserByPhone(phone);
        if (exist != null) {
            Toast.makeText(this, "手机号已注册", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = new User(phone, password);
        userDao.insertUser(user);
        Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}