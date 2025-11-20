package com.uilover.project2002.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
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
import com.uilover.project2002.auth.LoginActivity
import com.uilover.project2002.auth.RegisterActivity
import com.uilover.project2002.databinding.ActivityMainBinding
import com.uilover.project2002.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private val sliderHandler = Handler()
    private val sliderRunnable = Runnable {
        binding.viewPager2.currentItem = binding.viewPager2.currentItem + 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1️⃣ Khởi tạo binding trước khi đụng vào nó
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2️⃣ Firebase Auth
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // 3️⃣ Xử lý login / logout
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

        // 4️⃣ Firebase Database
        database = FirebaseDatabase.getInstance()

        // 5️⃣ Cấu hình giao diện toàn màn hình
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        // 6️⃣ Khởi tạo các phần khác
        initBanner()
        initTopMoving()
        initUpcomming()

// ✅ Sửa lại code xử lý ChipNavigationBar
        val bottomNav = binding.bottomNav
        bottomNav.setOnItemSelectedListener { id ->
            when (id) {
                // Giả sử id của item trang chủ trong bottom_menu.xml là 'explorer'
                R.id.explorer -> {
                    // Không cần làm gì nếu đã ở MainActivity
                }

                // Đây là phần quan trọng
                R.id.favorites -> {
                    val intent = Intent(this, FavoriteFilmsActivity::class.java)
                    startActivity(intent)
                }

                R.id.cart -> {
                    // Xử lý khi nhấn vào giỏ hàng
                }

                R.id.profile -> {
                    // Xử lý khi nhấn vào hồ sơ
                }
            }
        }


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
}