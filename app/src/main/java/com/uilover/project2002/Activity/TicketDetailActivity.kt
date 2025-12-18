package com.uilover.project2002.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uilover.project2002.databinding.ActivityTicketDetailBinding

class TicketDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTicketDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nhận dữ liệu
        val movie = intent.getStringExtra("movie") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val time = intent.getStringExtra("time") ?: ""
        val room = intent.getStringExtra("room") ?: ""
        val seats = intent.getStringExtra("seats") ?: ""
        val price = intent.getDoubleExtra("price", 0.0)

        // Gán lên UI
        binding.txtMovie.text = movie
        binding.txtDate.text = date
        binding.txtTime.text = time
        binding.txtRoom.text = room
        binding.txtSeats.text = seats
        binding.txtPrice.text = "$price đ"

        // Nút thanh toán
        binding.btnPayment.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("amount", price)
                putExtra("seats", seats)
                putExtra("movie", movie)
                putExtra("date", date)
                putExtra("time", time)
                putExtra("room", room)
            }
            startActivity(intent)
            // Đóng activity này để khi quay lại sẽ về màn chọn ghế
            finish()
        }
    }
}