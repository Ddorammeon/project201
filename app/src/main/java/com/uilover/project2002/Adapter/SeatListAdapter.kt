package com.uilover.project2002.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uilover.project2002.Models.Seat
import com.uilover.project2002.R
import com.uilover.project2002.databinding.SeatItemBinding

class SeatListAdapter(
    private val seatList: List<Seat>,
    private val context: Context,
    private val selectedSeat: SelectedSeat
) : RecyclerView.Adapter<SeatListAdapter.SeatViewholder>() {

    val selectedSeatName = ArrayList<String>()

    class SeatViewholder(val binding: SeatItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewholder {
        return SeatViewholder(
            SeatItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SeatViewholder, position: Int) {
        val seat = seatList[position]
        holder.binding.seat.text = seat.name

        when (seat.status) {
            Seat.SeatStatus.AVAILABLE -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_seat_available)
                holder.binding.seat.setTextColor(context.getColor(R.color.white))
            }
            Seat.SeatStatus.SELECTED -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_seat_selected)
                holder.binding.seat.setTextColor(context.getColor(R.color.black))
            }
            Seat.SeatStatus.UNAVAILABLE -> {
                holder.binding.seat.setBackgroundResource(R.drawable.ic_seat_unavailable)
                holder.binding.seat.setTextColor(context.getColor(R.color.grey))
            }
        }

        holder.binding.seat.setOnClickListener {
            when (seat.status) {
                Seat.SeatStatus.AVAILABLE -> {
                    seat.status = Seat.SeatStatus.SELECTED
                    selectedSeatName.add(seat.name)
                    notifyItemChanged(position)
                    selectedSeat.onSeatLocked(seat.name)  // <<<< THÊM DÒNG NÀY
                }
                Seat.SeatStatus.SELECTED -> {
                    seat.status = Seat.SeatStatus.AVAILABLE
                    selectedSeatName.remove(seat.name)
                    notifyItemChanged(position)
                    selectedSeat.onSeatUnlocked(seat.name)  // <<<< THÊM DÒNG NÀY
                }
                else -> {
                    // Ghế không khả dụng - không làm gì
                }
            }
            val selected = selectedSeatName.joinToString(",")
            selectedSeat.Return(selected, selectedSeatName.size)
        }
    }

    override fun getItemCount(): Int = seatList.size

    // Thêm method để update trạng thái ghế từ server
    fun updateSeatStatus(bookedSeats: List<String>, lockedSeats: List<String>) {
        seatList.forEachIndexed { index, seat ->
            val seatName = seat.name

            when {
                bookedSeats.contains(seatName) -> {
                    // Ghế đã được thanh toán → UNAVAILABLE
                    seat.status = Seat.SeatStatus.UNAVAILABLE
                }
                lockedSeats.contains(seatName) -> {
                    // <<<< SỬA ĐOẠN NÀY >>>>
                    // Nếu ghế đang locked NHƯNG là ghế mình đã chọn → giữ nguyên SELECTED
                    if (selectedSeatName.contains(seatName)) {
                        seat.status = Seat.SeatStatus.SELECTED
                    } else {
                        // Ghế locked bởi user khác → UNAVAILABLE
                        seat.status = Seat.SeatStatus.UNAVAILABLE
                    }
                }
                else -> {
                    // Ghế trống → chỉ set AVAILABLE nếu chưa được user hiện tại chọn
                    if (selectedSeatName.contains(seatName)) {
                        seat.status = Seat.SeatStatus.SELECTED
                    } else {
                        seat.status = Seat.SeatStatus.AVAILABLE
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    interface SelectedSeat {
        fun Return(selectedName: String, num: Int)
        fun onSeatLocked(seatName: String)      // <<<< THÊM
        fun onSeatUnlocked(seatName: String)
    }
}