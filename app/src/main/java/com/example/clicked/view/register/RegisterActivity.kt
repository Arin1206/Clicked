package com.example.clicked.view.register

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clicked.R
import com.example.clicked.databinding.ActivityRegisterBinding
import com.example.clicked.view.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = Firebase.auth

        binding.submitregis.setOnClickListener {
            val email = binding.edRegisEmail.text.toString().trim()
            val username = binding.edRegisUsername.text.toString().trim()
            val password = binding.edRegisPassword.text.toString().trim()
            val passwordconfirm = binding.edRegisPasswordconfirm.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || passwordconfirm.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordconfirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6){
                Toast.makeText(this, "Passwords at least 6 character", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user with email and password
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Get the authenticated user's ID
                        val user = firebaseAuth.currentUser
                        val userId = user?.uid

                        val userMap = hashMapOf(
                            "name" to username,
                            "email" to email,
                            "password" to password
                        )

                        if (userId != null) {
                            db.collection("users").document(userId).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Successfully registered", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()

                                }
                                .addOnFailureListener { exception ->
                                    Log.w(TAG, "Error adding document", exception)
                                }
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
