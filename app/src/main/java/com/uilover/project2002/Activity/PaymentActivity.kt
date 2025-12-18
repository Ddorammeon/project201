package com.uilover.project2002.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.uilover.project2002.databinding.ActivityPaymentBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val client = OkHttpClient()
    private var orderCode: String = ""
    private val handler = Handler(Looper.getMainLooper())
    private var checkPaymentRunnable: Runnable? = null
    private val CHECK_INTERVAL = 3000L // Kiểm tra mỗi 3 giây
    private val TIMEOUT_DURATION = 10 * 60 * 1000L // 10 phút

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val amount = intent.getDoubleExtra("amount", 0.0)
        val seats = intent.getStringExtra("seats") ?: "unknown"
        val movie = intent.getStringExtra("movie") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val time = intent.getStringExtra("time") ?: ""
        val room = intent.getStringExtra("room") ?: ""

        // Lấy userId từ SharedPreferences hoặc session
        val userId = getUserId() // Implement hàm này

        // Gọi API server tạo QR PayOS
        createPayment(amount, seats, userId, movie, date, time, room)

        // Bắt đầu đếm ngược 10 phút
        handler.postDelayed({
            onPaymentTimeout()
        }, TIMEOUT_DURATION)
    }

    private fun getUserId(): String {
        // Lấy userId từ SharedPreferences
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        return firebaseUser?.uid ?: "guest_${System.currentTimeMillis()}"
    }

    private fun createPayment(amount: Double, seats: String, userId: String,
                              movie: String, date: String, time: String, room: String) {
        // PayOS giới hạn description tối đa 25 ký tự
        val shortSeats = seats.split(",").take(2).joinToString(",")
        val description = "Ghe $shortSeats".take(25) // Cắt tối đa 25 ký tự

        val json = JSONObject()
        json.put("amount", amount.toInt())
        json.put("description", description)
        json.put("seats", seats)
        json.put("userId", userId)
        json.put("movie", movie)
        json.put("date", date)
        json.put("time", time)
        json.put("room", room)

        val JSON = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(JSON, json.toString())

        val request = Request.Builder()
            .url("http://10.0.2.2:3001/create-payment")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@PaymentActivity, "Lỗi tạo QR: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val resString = response.body()?.string() ?: ""

                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@PaymentActivity, "Lỗi: $resString", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    return
                }

                val jsonRes = JSONObject(resString)
                val checkoutUrl = jsonRes.getString("checkoutUrl")
                orderCode = jsonRes.getString("orderCode")

                runOnUiThread {
                    setupQR(checkoutUrl)
                    startCheckingPaymentStatus()
                }
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupQR(url: String) {
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.webViewClient = WebViewClient()
        binding.webview.loadUrl(url)
    }

    private fun startCheckingPaymentStatus() {
        checkPaymentRunnable = object : Runnable {
            override fun run() {
                checkPaymentStatus()
                handler.postDelayed(this, CHECK_INTERVAL)
            }
        }
        handler.post(checkPaymentRunnable!!)
    }

    private fun checkPaymentStatus() {
        val request = Request.Builder()
            .url("http://10.0.2.2:3001/check-payment/$orderCode")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Không làm gì, sẽ thử lại lần sau
            }

            override fun onResponse(call: Call, response: Response) {
                val resString = response.body()?.string() ?: ""
                val jsonRes = JSONObject(resString)
                val status = jsonRes.getString("status")

                runOnUiThread {
                    when (status) {
                        "paid" -> onPaymentSuccess()
                        "expired" -> onPaymentTimeout()
                        // "pending" -> tiếp tục kiểm tra
                    }
                }
            }
        })
    }

    private fun onPaymentSuccess() {
        // Dừng kiểm tra
        stopChecking()

        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("✅ Thanh toán thành công!")
                .setMessage("Vé của bạn đã được xác nhận. Cảm ơn bạn đã sử dụng dịch vụ!")
                .setCancelable(false)

                .setNegativeButton("Về trang chủ") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .show()
        }
    }

    private fun onPaymentTimeout() {
        // Dừng kiểm tra
        stopChecking()

        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("⏰ Hết thời gian")
                .setMessage("Mã QR đã hết hạn (10 phút). Ghế của bạn đã được mở khóa. Vui lòng chọn lại ghế.")
                .setCancelable(false)
                .setPositiveButton("Chọn lại ghế") { _, _ ->
                    // Quay về màn hình chọn ghế
                    val intent = Intent(this, SeatListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
                .show()
        }
    }

    private fun stopChecking() {
        checkPaymentRunnable?.let {
            handler.removeCallbacks(it)
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopChecking()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Hủy thanh toán?")
            .setMessage("Bạn có chắc muốn hủy thanh toán? Ghế sẽ được mở khóa sau 10 phút.")
            .setPositiveButton("Có, hủy") { _, _ ->
                super.onBackPressed()
                finish()
            }
            .setNegativeButton("Không", null)
            .show()
    }
}