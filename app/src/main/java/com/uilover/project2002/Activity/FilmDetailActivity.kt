package com.uilover.project2002.Activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.uilover.project2002.Adapter.CastListAdapter
import com.uilover.project2002.Adapter.CategoryEachFilmAdapter
import com.uilover.project2002.Models.Film
import com.uilover.project2002.R
import com.uilover.project2002.databinding.ActivityFilmDetailBinding
import eightbitlab.com.blurview.RenderScriptBlur

class FilmDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilmDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var currentFilm: Film? = null
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilmDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setVariable()
    }

    private fun setVariable() {
        val item: Film = intent.getParcelableExtra("object")!!
        currentFilm = item

        val requestOptions =
            RequestOptions().transform(CenterCrop(), GranularRoundedCorners(0f, 0f, 50f, 50f))

        Glide.with(this)
            .load(item.Poster)
            .apply(requestOptions)
            .into(binding.filmPic)

        binding.titleTxt.text = item.Title
        binding.imdbTxt.text = "IMDB ${item.Imdb}"
        binding.movieTimeTxt.text = "${item.Year} - ${item.Time}"
        binding.movieSummeryTxt.text = item.Description

        // Kiểm tra trạng thái yêu thích
        checkFavoriteStatus(item)

        binding.backBtn.setOnClickListener {
            finish()
        }

        // Xử lý bookmark
        binding.imageView5.setOnClickListener {
            toggleFavorite(item)
        }

        binding.buyTicketBtn.setOnClickListener {
            val intent = Intent(this, SeatListActivity::class.java)
            intent.putExtra("film", item)
            startActivity(intent)
        }

        val radius = 10f
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowsBackground = decorView.background

        binding.blurView.setupWith(rootView, RenderScriptBlur(this))
            .setFrameClearDrawable(windowsBackground)
            .setBlurRadius(radius)
        binding.blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.blurView.clipToOutline = true

        item.Genre?.let {
            binding.genreView.adapter = CategoryEachFilmAdapter(it)
            binding.genreView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }

        item.Casts?.let {
            binding.castListView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.castListView.adapter = CastListAdapter(it)
        }
    }

    private fun checkFavoriteStatus(film: Film) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            updateBookmarkIcon(false)
            return
        }

        // Sử dụng Title làm key vì Items không có ID
        val filmTitle = film.Title ?: return

        database.child("Users")
            .child(currentUser.uid)
            .child("favorites")
            .child(filmTitle)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFavorite = snapshot.exists()
                    updateBookmarkIcon(isFavorite)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Không hiển thị lỗi để tránh làm phiền user
                }
            })
    }

    private fun toggleFavorite(film: Film) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login to save favorites", Toast.LENGTH_SHORT).show()
            return
        }

        val filmTitle = film.Title ?: return

        val favoritesRef = database.child("Users")
            .child(currentUser.uid)
            .child("favorites")
            .child(filmTitle)

        if (isFavorite) {
            // Xóa khỏi yêu thích
            favoritesRef.removeValue()
                .addOnSuccessListener {
                    isFavorite = false
                    updateBookmarkIcon(false)
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to remove", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Thêm vào yêu thích - lưu giá trị true
            favoritesRef.setValue(true)
                .addOnSuccessListener {
                    isFavorite = true
                    updateBookmarkIcon(true)
                    Toast.makeText(this, "Added to favorites ❤️", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateBookmarkIcon(isFavorited: Boolean) {
        if (isFavorited) {
            // Icon bookmark đã lưu (màu cam)
            binding.imageView5.setColorFilter(
                ContextCompat.getColor(this, R.color.orange),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            // Icon bookmark chưa lưu (màu trắng)
            binding.imageView5.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }
}