package com.example.clicked.view.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clicked.R
import com.example.clicked.view.login.LoginActivity
import com.example.clicked.view.main.MainActivity
import com.example.clicked.view.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)


        firebaseAuth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun onButtonLoginClick(view: android.view.View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun onButtonRegisterClick(view: android.view.View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
