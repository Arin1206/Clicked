package com.example.clicked.view.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.clicked.R
import com.example.clicked.databinding.ActivityMainBinding
import com.example.clicked.view.Update.UpdateFragment
import com.example.clicked.view.home.HomeFragment
import com.example.clicked.view.maps.MapsFragment
import com.example.clicked.view.profile.ProfileFragment
import com.example.clicked.view.setting.SettingFragment
import com.example.clicked.view.upload.UploadFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    internal lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableFullscreen()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        replaceFragment(HomeFragment())
        firebaseAuth = FirebaseAuth.getInstance()
//        binding.submitlogout.setOnClickListener{
//            firebaseAuth.signOut()
//            val intent = Intent(this, WelcomeActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
        binding.bottomnavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.homepage -> replaceFragment(HomeFragment())
                R.id.upload -> replaceFragment(UploadFragment())
                R.id.maps -> replaceFragment(MapsFragment())
                R.id.setting -> replaceFragment(SettingFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
                else -> { }
            }
            true
        }
    }


    private fun enableFullscreen() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        actionBar?.hide()
    }

    fun replaceFragment(fragment: Fragment, hideBottomNavigation: Boolean = false) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame, fragment)

        if (hideBottomNavigation) {
            binding.bottomnavigation.visibility = View.GONE
        } else {
            binding.bottomnavigation.visibility = View.VISIBLE
        }

        fragmentTransaction.commit()
    }


}
