package com.example.clicked.view.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.clicked.R
import com.example.clicked.databinding.ActivityMainBinding
import com.example.clicked.view.home.HomeFragment
import com.example.clicked.view.maps.MapsFragment
import com.example.clicked.view.profile.ProfileFragment
import com.example.clicked.view.setting.SettingFragment
import com.example.clicked.view.upload.UploadFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        replacefragment(HomeFragment())
        firebaseAuth = FirebaseAuth.getInstance()
//        binding.submitlogout.setOnClickListener{
//            firebaseAuth.signOut()
//            val intent = Intent(this, WelcomeActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
        binding.bottomnavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.homepage->replacefragment(HomeFragment())
                R.id.upload->replacefragment(UploadFragment())
                R.id.maps->replacefragment(MapsFragment())
                R.id.setting->replacefragment(SettingFragment())
                R.id.profile->replacefragment(ProfileFragment())
                else->{

                }
            }
            true
        }

    }

    private fun replacefragment(fragment:Fragment){
        val fragmentManager = supportFragmentManager
        val fragmenttransaction = fragmentManager.beginTransaction()
        fragmenttransaction.replace(R.id.frame,fragment)
        fragmenttransaction.commit()
    }


}