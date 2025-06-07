package com.gxuwz.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gxuwz.app.R;
import com.gxuwz.app.dao.UserDao;
import com.gxuwz.app.db.AppDatabase;
import com.gxuwz.app.model.bean.User;

public class LoginActivity extends AppCompatActivity {

    private EditText etAccount, etPassword, etCode;
    private Button btnSendCode, btnLogin;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        etCode = findViewById(R.id.et_code);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnLogin = findViewById(R.id.btn_login);

        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        View  tvToRegister = findViewById(R.id.tv_to_register);
        tvToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
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

    private void login() {
        String phone = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone) || phone.length() != 11) {
            etAccount.setError("请输入正确的手机号");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            return;
        }

        // Room数据库操作
        AppDatabase db = AppDatabase.getInstance(this);
        UserDao userDao = db.userDao();
        User user = userDao.getUserByPhone(phone);

        if (user == null) {
            Toast.makeText(this, "手机号或密码错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(user.password)) {
            Toast.makeText(this, "手机号或密码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 登录成功
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
        // TODO: 跳转主页或保存登录状态
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
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
