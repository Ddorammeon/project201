package com.uilover.project2002.booking



import com.google.gson.annotations.SerializedName

data class BookingResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("bookings")
    val bookings: List<Booking>,

    @SerializedName("count")
    val count: Int
)

data class Booking(
    @SerializedName("id")
    val id: Int,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("movie_title")
    val movieTitle: String,

    @SerializedName("show_date")
    val showDate: String,

    @SerializedName("show_time")
    val showTime: String,

    @SerializedName("room")
    val room: String,

    @SerializedName("seat_numbers")
    val seatNumbers: List<String>,

    @SerializedName("total_price")
    val totalPrice: String,

    @SerializedName("payment_id")
    val paymentId: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("created_at")
    val createdAt: String
)
