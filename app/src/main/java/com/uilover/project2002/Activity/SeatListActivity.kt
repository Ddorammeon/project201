package com.uilover.project2002.Activity

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.uilover.project2002.Adapter.DateAdapter
import com.uilover.project2002.Adapter.SeatListAdapter
import com.uilover.project2002.Adapter.TimeAdapter
import com.uilover.project2002.Models.Film
import com.uilover.project2002.Models.RoomInfo
import com.uilover.project2002.Models.Seat
import com.uilover.project2002.Models.Showtime
import com.uilover.project2002.Retrofit.RetrofitInstance
import com.uilover.project2002.ServerModels.LockSeatRequest
import com.uilover.project2002.ServerModels.UnlockSeatRequest
import com.uilover.project2002.databinding.ActivitySeatListBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SeatListActivity : AppCompatActivity(),
    DateAdapter.OnDateSelectedListener,
    TimeAdapter.OnTimeSelectedListener {

    private lateinit var binding: ActivitySeatListBinding
    private lateinit var film: Film
    private lateinit var database: FirebaseDatabase

    private var price: Double = 0.0
    private var number: Int = 0
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var selectedRoom: String = ""
    private var showtimePrice: Double = 0.0

    private val availableDates = mutableListOf<String>()
    private val availableTimes = mutableListOf<String>()
    private var dateAdapter: DateAdapter? = null
    private var timeAdapter: TimeAdapter? = null
    private var seatAdapter: SeatListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        getIntentExtra()
        setVariable()
        loadAvailableDates()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun loadAvailableDates() {
        binding.dateRecyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val showtimesRef = database.getReference("showtimes/${film.Title}")
        showtimesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                availableDates.clear()
                for (dateSnapshot in snapshot.children) {
                    dateSnapshot.key?.let { availableDates.add(it) }
                }

                if (availableDates.isNotEmpty()) {
                    // Convert dates to display format
                    val displayDates = availableDates.map { formatDate(it) }
                    dateAdapter = DateAdapter(displayDates, this@SeatListActivity)
                    binding.dateRecyclerview.adapter = dateAdapter
                } else {
                    Toast.makeText(
                        this@SeatListActivity,
                        "Không có suất chiếu",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeatListActivity", "Error loading dates: ${error.message}")
                Toast.makeText(
                    this@SeatListActivity,
                    "Lỗi tải dữ liệu",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun loadAvailableTimes(date: String) {
        binding.TimeRecyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val timesRef = database.getReference("showtimes/${film.Title}/$date")
        timesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                availableTimes.clear()
                for (timeSnapshot in snapshot.children) {
                    timeSnapshot.key?.let { availableTimes.add(it) }
                }

                if (availableTimes.isNotEmpty()) {
                    timeAdapter = TimeAdapter(availableTimes, this@SeatListActivity)
                    binding.TimeRecyclerview.adapter = timeAdapter
                } else {
                    Toast.makeText(
                        this@SeatListActivity,
                        "Không có giờ chiếu",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeatListActivity", "Error loading times: ${error.message}")
            }
        })
    }

    private fun loadShowtimeAndSeats(date: String, time: String) {
        val showtimeRef = database.getReference("showtimes/${film.Title}/$date/$time")
        showtimeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val showtime = snapshot.getValue(Showtime::class.java)
                if (showtime != null) {
                    selectedRoom = showtime.room
                    showtimePrice = showtime.price
                    loadRoomLayout(showtime.room)
                } else {
                    Toast.makeText(
                        this@SeatListActivity,
                        "Không tìm thấy thông tin suất chiếu",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeatListActivity", "Error loading showtime: ${error.message}")
            }
        })
    }

    private fun loadRoomLayout(roomName: String) {
        val roomRef = database.getReference("rooms/$roomName")
        roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val seatLayoutSnap = snapshot.child("seatLayout")
                Log.d("DEBUG", "seatLayout raw: ${seatLayoutSnap.value}")
                val roomInfo = snapshot.getValue(RoomInfo::class.java)
                if (roomInfo != null) {
                    initSeatsList(roomInfo)
                } else {
                    Toast.makeText(
                        this@SeatListActivity,
                        "Không tìm thấy thông tin phòng",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeatListActivity", "Error loading room: ${error.message}")
            }
        })
    }

    private fun initSeatsList(roomInfo: RoomInfo) {
        val gridLayoutManager = GridLayoutManager(this, 7)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position % 7 == 3) 1 else 1
            }
        }

        binding.seatRecyclerview.layoutManager = gridLayoutManager

        val seatList = mutableListOf<Seat>()
        val numberSeats = roomInfo.totalSeats

        // Tạo danh sách ghế với tên từ seatLayout
        for (i in 1..numberSeats) {
            val seatName = roomInfo.seatLayout.getOrNull(i) ?: i.toString()

            // Mặc định tất cả ghế AVAILABLE, sẽ update sau khi gọi API
            seatList.add(Seat(Seat.SeatStatus.AVAILABLE, seatName))
        }

        seatAdapter = SeatListAdapter(seatList, this, object : SeatListAdapter.SelectedSeat {
            override fun Return(selectedName: String, num: Int) {
                binding.numberSelectedTxt.text = "$num Seat Selected"
                val df = DecimalFormat("#.##")
                price = df.format(num * showtimePrice).toDouble()
                number = num
                binding.priceTxt.text = "$$price"

                // TODO: Gọi API để lock ghế ở đây
                // lockSeatsOnServer(selectedName.split(","))
            }
            override fun onSeatLocked(seatName: String) {
                lifecycleScope.launch {
                    try {
                        val seats = seatAdapter?.selectedSeatName?.toList() ?: return@launch
                        val req = LockSeatRequest(
                            movie = film.Title,
                            date = selectedDate,
                            time = selectedTime,
                            room = selectedRoom,
                            seats = seats,
                            userId = "USER123" // TODO: truyền id thật
                        )
                        RetrofitInstance.api.lockSeats(req)
                        loadSeatStatus() // refresh UI
                    } catch (e: Exception) {
                        Log.e("SeatList", "Lock error: ${e.message}")
                    }
                }
            }

            override fun onSeatUnlocked(seatName: String) {
                lifecycleScope.launch {
                    try {
                        val req = UnlockSeatRequest(
                            movie = film.Title,
                            date = selectedDate,
                            time = selectedTime,
                            room = selectedRoom,
                            seats = listOf(seatName),
                            userId = "USER123" // TODO: truyền id thật
                        )
                        RetrofitInstance.api.unlockSeats(req)
                        loadSeatStatus() // refresh UI
                    } catch (e: Exception) {
                        Log.e("SeatList", "Unlock error: ${e.message}")
                    }
                }
            }
        })

        binding.seatRecyclerview.adapter = seatAdapter
        binding.seatRecyclerview.isNestedScrollingEnabled = false

        // Sau khi render ghế, gọi API để lấy trạng thái ghế
        loadSeatStatus()
    }

    private fun loadSeatStatus() {
        // TODO: Gọi API server của bạn để lấy trạng thái ghế
        // GET /api/seats/status?movie={film.title}&date={selectedDate}&time={selectedTime}&room={selectedRoom}

        /* Example:
        apiService.getSeatStatus(
            movie = film.title,
            date = selectedDate,
            time = selectedTime,
            room = selectedRoom
        ).enqueue(object : Callback<SeatStatusResponse> {
            override fun onResponse(call: Call<SeatStatusResponse>, response: Response<SeatStatusResponse>) {
                if (response.isSuccessful) {
                    val status = response.body()
                    status?.let {
                        updateSeatsUI(it.booked, it.locked)
                    }
                }
            }

            override fun onFailure(call: Call<SeatStatusResponse>, t: Throwable) {
                Log.e("SeatListActivity", "Error: ${t.message}")
            }
        })
        */

        // Giả lập data cho demo
        if (selectedDate.isEmpty() || selectedTime.isEmpty() || selectedRoom.isEmpty()) return

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getSeatStatus(
                    movie = film.Title,
                    date = selectedDate,
                    time = selectedTime,
                    room = selectedRoom
                )

                if (response.success) {
                    updateSeatsUI(response.booked, response.locked)
                }

            } catch (e: Exception) {
                Log.e("SeatListActivity", "Error: ${e.message}")
            }
        }
    }

    private fun updateSeatsUI(bookedSeats: List<String>, lockedSeats: List<String>) {
        seatAdapter?.updateSeatStatus(bookedSeats, lockedSeats)
    }

    override fun onDateSelected(position: Int, formattedDate: String) {
        if (position < availableDates.size) {
            selectedDate = availableDates[position]
            Log.d("SeatListActivity", "Selected date: $selectedDate")

            // Reset time selection
            selectedTime = ""
            availableTimes.clear()
            timeAdapter?.notifyDataSetChanged()

            // Load times for selected date
            loadAvailableTimes(selectedDate)
        }
    }

    override fun onTimeSelected(position: Int, time: String) {
        if (position < availableTimes.size) {
            selectedTime = availableTimes[position]
            Log.d("SeatListActivity", "Selected time: $selectedTime")

            // Load showtime details and seats
            if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                loadShowtimeAndSeats(selectedDate, selectedTime)
            }
        }
    }

    private fun formatDate(dateString: String): String {
        // Convert "2025-11-18" to "Mon/18/Nov"
        return try {
            val date = LocalDate.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("EEE/dd/MMM")
            date.format(formatter)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun setVariable() {
        binding.backBtn.setOnClickListener {
            finish()
        }
    }


    private fun getIntentExtra() {
        film = intent.getParcelableExtra("film")!!
    }
}