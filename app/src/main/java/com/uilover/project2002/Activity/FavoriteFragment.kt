package com.uilover.project2002.Activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.uilover.project2002.Adapter.FilmListAdapter
import com.uilover.project2002.Models.Film
import com.uilover.project2002.R
import com.uilover.project2002.databinding.ActivityFavoriteFilmsBinding

class FavoriteFragment : Fragment() {

    private var _binding: ActivityFavoriteFilmsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: FilmListAdapter
    private val favoriteFilms = ArrayList<Film>()
    private var favoritesListener: ValueEventListener? = null
    private var favoritesRef: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityFavoriteFilmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Back → chuyển về Home thông qua bottom nav
        binding.backBtn.setOnClickListener {
            (activity as? MainActivity)?.findViewById<com.ismaeldivita.chipnavigation.ChipNavigationBar>(R.id.bottomNav)
                ?.setItemSelected(R.id.explorer, true)
        }

        if (currentUser == null) {
            (activity as? MainActivity)?.findViewById<com.ismaeldivita.chipnavigation.ChipNavigationBar>(R.id.bottomNav)
                ?.setItemSelected(R.id.explorer, true)
            return
        }

        // RecyclerView
        adapter = FilmListAdapter(favoriteFilms)
        binding.favoriteFilmsRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), 2)
        binding.favoriteFilmsRecyclerView.adapter = adapter

        fetchFavoriteFilms(currentUser.uid)
    }

    private fun fetchFavoriteFilms(userId: String) {
        favoritesRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(userId)
            .child("favorites")

        val filmsRef = FirebaseDatabase.getInstance()
            .getReference("Items")

        // Lấy danh sách favorites trước
        favoritesListener = object : ValueEventListener {
            override fun onDataChange(favSnapshot: DataSnapshot) {
                // Kiểm tra binding còn tồn tại không
                if (_binding == null) return

                favoriteFilms.clear()

                if (!favSnapshot.exists()) {
                    adapter.notifyDataSetChanged()
                    showEmptyState(true)
                    return
                }

                // Lấy danh sách title phim yêu thích
                val favoriteTitles = mutableListOf<String>()
                for (filmSnapshot in favSnapshot.children) {
                    val filmTitle = filmSnapshot.key
                    if (filmTitle != null) {
                        favoriteTitles.add(filmTitle)
                    }
                }

                // Lấy tất cả phim từ Items
                filmsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(itemsSnapshot: DataSnapshot) {
                        // Kiểm tra binding còn tồn tại không
                        if (_binding == null) return

                        for (filmSnapshot in itemsSnapshot.children) {
                            val film = filmSnapshot.getValue(Film::class.java)
                            if (film != null && favoriteTitles.contains(film.Title)) {
                                favoriteFilms.add(film)
                            }
                        }
                        adapter.notifyDataSetChanged()
                        showEmptyState(favoriteFilms.isEmpty())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (_binding == null) return
                        showEmptyState(true)
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                if (_binding == null) return
                showEmptyState(true)
            }
        }

        favoritesRef?.addValueEventListener(favoritesListener!!)
    }

    private fun showEmptyState(isEmpty: Boolean) {
        // Kiểm tra binding trước khi sử dụng
        if (_binding == null) return

        if (isEmpty) {
            binding.favoriteFilmsRecyclerView.visibility = View.GONE
            // Nếu có TextView hoặc View hiển thị empty state thì show lên
            // binding.emptyStateView?.visibility = View.VISIBLE
        } else {
            binding.favoriteFilmsRecyclerView.visibility = View.VISIBLE
            // binding.emptyStateView?.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Remove listener để tránh memory leak
        if (favoritesRef != null && favoritesListener != null) {
            favoritesRef?.removeEventListener(favoritesListener!!)
        }

        _binding = null
    }
}