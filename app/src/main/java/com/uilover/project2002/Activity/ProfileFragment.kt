package com.uilover.project2002.Activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.uilover.project2002.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Hiển thị thông tin user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.tvUserEmail.text = currentUser.email
            // Lấy tên từ email (phần trước @)
            binding.tvUserName.text = currentUser.email?.substringBefore("@") ?: "User"
        }

        // Xử lý các button clicks
        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(context, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }

        binding.btnChangePassword.setOnClickListener {
            Toast.makeText(context, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }

        binding.btnLanguage.setOnClickListener {
            Toast.makeText(context, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(context, "Bật thông báo", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Tắt thông báo", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            // Quay về MainActivity và refresh
            activity?.recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}