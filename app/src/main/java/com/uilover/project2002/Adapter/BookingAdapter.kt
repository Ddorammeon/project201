package com.uilover.project2002.Adapter



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uilover.project2002.booking.Booking
import com.uilover.project2002.databinding.ItemBookingBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BookingAdapter(private val bookings: List<Booking>) :
    RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        with(holder.binding) {
            // Movie title
            movieTitleTxt.text = booking.movieTitle

            // Status
            statusTxt.text = booking.status.uppercase()

            // Format date and time
            val showDateTime = formatDateTime(booking.showDate, booking.showTime)
            showDateTimeTxt.text = showDateTime

            // Room
            roomTxt.text = booking.room.uppercase()

            // Seats
            seatsTxt.text = booking.seatNumbers.joinToString(", ")

            // Total price
            val formattedPrice = formatPrice(booking.totalPrice)
            totalPriceTxt.text = "Total: $formattedPrice"

            // Booking ID
            bookingIdTxt.text = "#${booking.id}"

            // Payment ID
            if (booking.paymentId != null) {
                paymentIdTxt.text = "Payment: ${booking.paymentId}"
            } else {
                paymentIdTxt.text = "Payment: N/A"
            }

            // Created at
            val createdDate = formatCreatedDate(booking.createdAt)
            createdAtTxt.text = "Booked on: $createdDate"
        }
    }

    override fun getItemCount() = bookings.size

    private fun formatDateTime(dateStr: String, timeStr: String): String {
        return try {
            // Parse date: "2025-11-23T17:00:00.000Z"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateStr)

            val outputDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val formattedDate = outputDateFormat.format(date!!)

            // Parse time: "10:00:00"
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val time = timeFormat.parse(timeStr)

            val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedTime = outputTimeFormat.format(time!!)

            "$formattedDate, $formattedTime"
        } catch (e: Exception) {
            "$dateStr, $timeStr"
        }
    }

    private fun formatCreatedDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateStr)

            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatPrice(priceStr: String): String {
        return try {
            val price = priceStr.toDouble()
            val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
            "${formatter.format(price)} đ"
        } catch (e: Exception) {
            "$priceStr đ"
        }
    }
}