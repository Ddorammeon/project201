package com.uilover.project2002.ServerModels

data class LockSeatResponse(
    val success: Boolean,
    val lockedSeats: List<String>,
    val failedSeats: List<FailedSeat>,
    val expiresAt: Long?
)