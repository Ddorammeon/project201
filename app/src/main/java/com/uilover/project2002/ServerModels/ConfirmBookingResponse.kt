package com.uilover.project2002.ServerModels

data class ConfirmBookingResponse(
    val success: Boolean,
    val bookingId: Int,
    val message: String
)