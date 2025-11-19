package com.uilover.project2002.ServerModels

data class LockSeatRequest(
    val movie: String?,
    val date: String,
    val time: String,
    val room: String,
    val seats: List<String>,
    val userId: String
)
