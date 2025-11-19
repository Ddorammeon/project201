package com.uilover.project2002.Retrofit



import com.uilover.project2002.ServerModels.ConfirmBookingRequest
import com.uilover.project2002.ServerModels.ConfirmBookingResponse
import com.uilover.project2002.ServerModels.LockSeatRequest
import com.uilover.project2002.ServerModels.LockSeatResponse
import com.uilover.project2002.ServerModels.SeatStatusResponse
import com.uilover.project2002.ServerModels.UnlockSeatRequest
import com.uilover.project2002.ServerModels.UnlockSeatResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    // 1. GET ghế (booked + locked)
    @GET("api/seats/status")
    suspend fun getSeatStatus(
        @Query("movie") movie: String?,
        @Query("date") date: String,
        @Query("time") time: String,
        @Query("room") room: String
    ): SeatStatusResponse

    // 2. Lock seats
    @POST("api/seats/lock")
    suspend fun lockSeats(@Body req: LockSeatRequest): LockSeatResponse

    // 3. Unlock seats
    @POST("api/seats/unlock")
    suspend fun unlockSeats(@Body req: UnlockSeatRequest): UnlockSeatResponse

    // 4. Confirm booking
    @POST("api/booking/confirm")
    suspend fun confirmBooking(@Body req: ConfirmBookingRequest): ConfirmBookingResponse
}

