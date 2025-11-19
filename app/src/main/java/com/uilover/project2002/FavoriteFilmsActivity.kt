package com.uilover.project2002

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.uilover.project2002.Adapter.FilmListAdapter // We will create or reuse an adapter
import com.uilover.project2002.Models.Film
import com.uilover.project2002.databinding.ActivityFavoriteFilmsBinding

class FavoriteFilmsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteFilmsBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: FilmListAdapter
    private val favoriteFilms = ArrayList<Film>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteFilmsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Handle back button click
        binding.backBtn.setOnClickListener { finish() }

        if (currentUser == null) {
            // If user is not logged in, we can finish the activity
            // Or redirect to login screen
            finish()
            return
        }

        // Setup RecyclerView
        adapter = FilmListAdapter(favoriteFilms)
        binding.favoriteFilmsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.favoriteFilmsRecyclerView.adapter = adapter

        // Fetch favorite films from Firebase
        fetchFavoriteFilms(currentUser.uid)
    }

    private fun fetchFavoriteFilms(userId: String) {
        val favoritesRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("favorites")
        val filmsRef = FirebaseDatabase.getInstance().getReference("Items") // Reference to the main films node

        favoritesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteFilms.clear()
                for (filmIdSnapshot in snapshot.children) {
                    val filmId = filmIdSnapshot.key
                    if (filmId != null) {
                        // For each favorite film ID, get the film details
                        filmsRef.child(filmId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(filmDataSnapshot: DataSnapshot) {
                                val film = filmDataSnapshot.getValue(Film::class.java)
                                if (film != null) {
                                    favoriteFilms.add(film)
                                    adapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
