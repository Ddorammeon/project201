package com.uilover.project2002.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.uilover.project2002.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Nút quay lại
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Nút gửi mã
        binding.btnSendCode.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gửi email đặt lại mật khẩu
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư",
                            Toast.LENGTH_LONG
                        ).show()
                        // Quay về màn hình đăng nhập
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Lỗi: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}