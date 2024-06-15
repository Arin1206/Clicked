package com.example.clicked.view.setting

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.example.clicked.R
import com.example.clicked.databinding.FragmentSettingBinding
import com.example.clicked.view.common.BaseFragment
import com.example.clicked.view.welcome.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {

    private lateinit var firebaseAuth: FirebaseAuth
    override fun setupUI() {

    }


    override fun setupListeners() {
        binding.submitlogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirmation_logout_title)
            .setMessage(R.string.confirmation_logout_message)
            .setPositiveButton(R.string.yes) { dialog, which ->
                logout()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun logout() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()
        val intent = Intent(requireContext(), WelcomeActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }


    override fun setupObservers() {
        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        binding.loadingProgressBar.visibility = View.VISIBLE

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
                    val imageUrl = document.getString("fileUrl")

                    binding.nameprofile.text = name
                    binding.emailprofile.text = email
                    binding.alamatprofile.text = address

                    imageUrl?.let {
                        Glide.with(requireContext())
                            .load(it)
                            .into(binding.imageprofile)
                    }
                    binding.loadingProgressBar.visibility =
                        View.GONE
                } else {
                    Log.d(TAG, "Document does not exist")
                    binding.loadingProgressBar.visibility =
                        View.GONE
                }
            }.addOnFailureListener { exception ->
                Log.d(TAG, "Error getting document: $exception")
                binding.loadingProgressBar.visibility = View.GONE
            }
        }
    }

}