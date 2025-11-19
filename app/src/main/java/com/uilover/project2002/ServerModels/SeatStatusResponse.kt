package com.uilover.project2002.ServerModels

data class SeatStatusResponse(
    val success: Boolean,
    val booked: List<String>,
    val locked: List<String>,
    val timestamp: Long
)
