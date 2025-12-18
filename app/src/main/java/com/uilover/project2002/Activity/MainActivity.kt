package com.uilover.project2002.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.uilover.project2002.Adapter.FilmListAdapter
import com.uilover.project2002.Adapter.SliderAdapter
import com.uilover.project2002.Models.Film
import com.uilover.project2002.Models.SliderItems
import com.uilover.project2002.R
import com.uilover.project2002.auth.LoginActivity
import com.uilover.project2002.auth.RegisterActivity
import com.uilover.project2002.databinding.ActivityMainBinding
import android.text.TextWatcher
import androidx.recyclerview.widget.GridLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private val sliderHandler = Handler()
    private val sliderRunnable = Runnable {
        binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Auth
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Xử lý login / logout
        if (currentUser != null) {
            binding.layoutUserInfo.visibility = View.VISIBLE
            binding.layoutLoginRegister.visibility = View.GONE
            binding.tvHelloUser.text = "Hi, ${currentUser.email}"

            binding.btnLogout.setOnClickListener {
                auth.signOut()
                recreate()
            }
        } else {
            binding.layoutUserInfo.visibility = View.GONE
            binding.layoutLoginRegister.visibility = View.VISIBLE

            binding.btnGotoLogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            binding.btnGotoRegister.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }

        // Firebase Database
        database = FirebaseDatabase.getInstance()

        // Cấu hình giao diện toàn màn hình
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Bottom Navigation setup
        binding.bottomNav.setOnItemSelectedListener { id ->
            when (id) {
                R.id.explorer -> {
                    showHomeContent()
                }

                R.id.favorites,
                R.id.cart,
                R.id.profile -> {

                    // ❌ CHƯA ĐĂNG NHẬP → CẤM VÀO
                    if (currentUser == null) {
                        binding.bottomNav.setItemSelected(R.id.explorer, true)

                        // Tuỳ chọn
                        Toast.makeText(
                            this,
                            "Vui lòng đăng nhập để sử dụng chức năng này",
                            Toast.LENGTH_SHORT
                        ).show()

                        // hoặc mở Login
                        // startActivity(Intent(this, LoginActivity::class.java))
                        return@setOnItemSelectedListener

                    }

                    // ✅ ĐÃ ĐĂNG NHẬP → CHO VÀO
                    when (id) {
                        R.id.favorites -> openFragment(FavoriteFragment())
                        R.id.cart -> openFragment(CartFragment())
                        R.id.profile -> openFragment(ProfileFragment())
                    }
                    true
                }
            }
            true
        }

        // Thêm chức năng tìm kiếm
        setupSearchFunction()

        // Hiển thị Home mặc định
        showHomeContent()
    }

    private fun setupSearchFunction() {
        binding.editTextText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isNotEmpty()) {
                    searchMovies(searchText)
                } else {
                    // Ẩn kết quả tìm kiếm, hiện lại home content
                    binding.searchResultsContainer.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun searchMovies(query: String) {
        val myRef: DatabaseReference = database.getReference("Items")

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val searchResults = ArrayList<Film>()

                // Tìm kiếm phim theo tên
                for (issue in snapshot.children) {
                    val movie = issue.getValue(Film::class.java)
                    if (movie?.Title?.contains(query, ignoreCase = true) == true) {
                        searchResults.add(movie)
                    }
                }

                // Hiển thị kết quả ngay trong MainActivity
                displaySearchResults(searchResults, query)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun displaySearchResults(results: ArrayList<Film>, query: String) {
        binding.searchResultsContainer.visibility = View.VISIBLE
        binding.tvSearchResultTitle.text = "Kết quả: \"$query\" (${results.size})"

        if (results.isEmpty()) {
            binding.recyclerViewSearch.visibility = View.GONE
            binding.tvNoResults.visibility = View.VISIBLE
        } else {
            binding.recyclerViewSearch.visibility = View.VISIBLE
            binding.tvNoResults.visibility = View.GONE

            // Dùng GridLayoutManager với 2 cột để gọn hơn
            binding.recyclerViewSearch.layoutManager = GridLayoutManager(this, 2)
            binding.recyclerViewSearch.adapter = FilmListAdapter(results)
        }
    }
    private fun showSearchResults(results: ArrayList<Film>, query: String) {
        // Ẩn home content, hiện search results
        binding.scrollViewHome.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        // Tạo fragment hiển thị kết quả tìm kiếm
        val searchFragment = SearchResultFragment.newInstance(results, query)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, searchFragment)
            .commit()
    }


    private fun showHomeContent() {
        // Ẩn fragment container, hiện home content
        binding.fragmentContainer.visibility = View.GONE
        binding.scrollViewHome.visibility = View.VISIBLE

        // Xóa back stack nếu có
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Load lại home content nếu chưa load
        if (binding.viewPager2.adapter == null) {
            initBanner()
            initTopMoving()
            initUpcomming()
        }
    }

    private fun openFragment(fragment: Fragment) {
        // Ẩn home content, hiện fragment
        binding.scrollViewHome.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        // Replace fragment không addToBackStack
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,  // enter
                android.R.anim.fade_out  // exit
            )
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun initTopMoving() {
        val myRef: DatabaseReference = database.getReference("Items")
        binding.progressBarTopMovies.visibility = View.VISIBLE
        val items = ArrayList<Film>()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (issue in snapshot.children) {
                        items.add(issue.getValue(Film::class.java)!!)
                    }
                    if (items.isNotEmpty()) {
                        binding.recyclerViewTopMovies.layoutManager = LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        binding.recyclerViewTopMovies.adapter = FilmListAdapter(items)
                    }
                    binding.progressBarTopMovies.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun initUpcomming() {
        val myRef: DatabaseReference = database.getReference("Upcomming")
        binding.progressBarupcomming.visibility = View.VISIBLE
        val items = ArrayList<Film>()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (issue in snapshot.children) {
                        items.add(issue.getValue(Film::class.java)!!)
                    }
                    if (items.isNotEmpty()) {
                        binding.recyclerViewUpcomming.layoutManager = LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        binding.recyclerViewUpcomming.adapter = FilmListAdapter(items)
                    }
                    binding.progressBarupcomming.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun initBanner() {
        val myRef: DatabaseReference = database.getReference("Banners")
        binding.progressBarSlider.visibility = View.VISIBLE

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<SliderItems>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(SliderItems::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                binding.progressBarSlider.visibility = View.GONE
                banners(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun banners(lists: MutableList<SliderItems>) {
        binding.viewPager2.adapter = SliderAdapter(lists, binding.viewPager2)
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.offscreenPageLimit = 3
        binding.viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer(ViewPager2.PageTransformer { page, position ->
                val r = 1 - Math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
            })
        }

        binding.viewPager2.setPageTransformer(compositePageTransformer)
        binding.viewPager2.currentItem = 1
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 2000)
    }

    override fun onBackPressed() {
        // Nếu đang ở fragment, quay về Home
        if (binding.fragmentContainer.visibility == View.VISIBLE) {
            binding.bottomNav.setItemSelected(R.id.explorer, true)
            showHomeContent()
        } else {
            super.onBackPressed()
        }
    }
}