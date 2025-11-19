package com.uilover.project2002.ServerModels

data class UnlockSeatRequest(
    val movie: String?,
    val date: String,
    val time: String,
    val room: String,
    val seats: List<String>,
    val userId: String
)