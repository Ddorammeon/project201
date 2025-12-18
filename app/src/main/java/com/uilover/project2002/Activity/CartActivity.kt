package com.uilover.project2002.Activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.uilover.project2002.Adapter.BookingAdapter
import com.uilover.project2002.booking.BookingResponse
import com.uilover.project2002.Retrofit.RetrofitInstance
import com.uilover.project2002.databinding.FragmentCartBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            showEmptyState()
            Toast.makeText(requireContext(), "Please login to view bookings", Toast.LENGTH_SHORT).show()
            return
        }

        loadBookings(currentUser.uid)
    }

    private fun loadBookings(userId: String) {
        showLoading()

        Log.d("CartFragment", "Loading bookings for user: $userId")

        RetrofitInstance.api.getUserBookings(userId).enqueue(object : Callback<BookingResponse> {
            override fun onResponse(
                call: Call<BookingResponse>,
                response: Response<BookingResponse>
            ) {
                hideLoading()

                if (response.isSuccessful) {
                    val bookingResponse = response.body()

                    Log.d("CartFragment", "Response successful: ${bookingResponse?.success}")
                    Log.d("CartFragment", "Bookings count: ${bookingResponse?.count}")

                    if (bookingResponse != null && bookingResponse.success) {
                        val bookings = bookingResponse.bookings

                        if (bookings.isEmpty()) {
                            showEmptyState()
                        } else {
                            showBookings(bookings)
                        }
                    } else {
                        showError("Failed to load bookings")
                    }
                } else {
                    Log.e("CartFragment", "Response not successful: ${response.code()}")
                    Log.e("CartFragment", "Error body: ${response.errorBody()?.string()}")
                    showError("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                hideLoading()
                Log.e("CartFragment", "API call failed", t)
                showError("Network error: ${t.message}")
                Toast.makeText(
                    requireContext(),
                    "Failed to connect to server",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showBookings(bookings: List<com.uilover.project2002.booking.Booking>) {
        if (_binding == null) return

        binding.bookingsRecyclerView.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        binding.errorTxt.visibility = View.GONE

        val adapter = BookingAdapter(bookings)
        binding.bookingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.bookingsRecyclerView.adapter = adapter

        Log.d("CartFragment", "Displaying ${bookings.size} bookings")
    }

    private fun showEmptyState() {
        if (_binding == null) return

        binding.bookingsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.errorTxt.visibility = View.GONE
    }

    private fun showError(message: String) {
        if (_binding == null) return

        binding.bookingsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        binding.errorTxt.visibility = View.VISIBLE
        binding.errorTxt.text = message
    }

    private fun showLoading() {
        if (_binding == null) return

        binding.progressBar.visibility = View.VISIBLE
        binding.bookingsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        binding.errorTxt.visibility = View.GONE
    }

    private fun hideLoading() {
        if (_binding == null) return

        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}