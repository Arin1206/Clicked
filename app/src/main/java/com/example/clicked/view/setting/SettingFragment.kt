package com.example.clicked.view.setting

import android.content.Intent
import com.example.clicked.databinding.FragmentSettingBinding
import com.example.clicked.view.common.BaseFragment
import com.example.clicked.view.welcome.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth


class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {

    private lateinit var firebaseAuth: FirebaseAuth
    override fun setupUI() {

    }


    override fun setupListeners() {
        binding.submitlogout.setOnClickListener {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth.signOut()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            startActivity(intent)
        }
    }


    override fun setupObservers() {

    }
}