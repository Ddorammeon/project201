package com.uilover.project2002.ServerModels

data class UnlockSeatResponse(
    val success: Boolean,
    val unlockedSeats: List<String>
)