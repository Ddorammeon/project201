package com.uilover.project2002.ServerModels

data class ConfirmBookingRequest(
    val movie: String,
    val date: String,
    val time: String,
    val room: String,
    val seats: List<String>,
    val userId: String,
    val paymentId: String?,
    val totalPrice: Double
)
