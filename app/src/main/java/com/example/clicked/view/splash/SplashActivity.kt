package com.example.clicked.view.splash

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.clicked.R
import com.example.clicked.view.welcome.WelcomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            delay(3000L)
            val intent = Intent(this@SplashActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}