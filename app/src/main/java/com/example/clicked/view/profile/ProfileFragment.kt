package com.example.clicked.view.profile

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.clicked.R
import com.example.clicked.databinding.FragmentProfileBinding
import com.example.clicked.databinding.FragmentSettingBinding
import com.example.clicked.view.common.BaseFragment
import com.example.clicked.view.welcome.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate)  {
    private lateinit var firebaseAuth: FirebaseAuth
    override fun setupUI() {

    }


    override fun setupListeners() {

    }


    override fun setupObservers() {
        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        binding.loadingProgressBar.visibility = View.VISIBLE // Show ProgressBar
        // Mengambil data dari Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)
            userRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    val email = document.getString("email")
                    val address = document.getString("address")
                    val imageUrl = document.getString("fileUrl") // Ambil URL gambar dari Firestore

                    // Tampilkan data di UI
                    binding.nameprof.text = name
                    binding.emailprof.text = email
                    binding.alamatprof.text = address

                    // Tampilkan gambar di ImageView jika URL gambar tersedia
                    imageUrl?.let {
                        Glide.with(requireContext())
                            .load(it)
                            .into(binding.imageprofile2)
                    }
                    binding.loadingProgressBar.visibility = View.GONE // Hide ProgressBar after loading data
                } else {
                    Log.d(ContentValues.TAG, "Document does not exist")
                    binding.loadingProgressBar.visibility = View.GONE // Hide ProgressBar if document does not exist
                }
            }.addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting document: $exception")
                binding.loadingProgressBar.visibility = View.GONE // Hide ProgressBar on failure
            }
        }
    }
}