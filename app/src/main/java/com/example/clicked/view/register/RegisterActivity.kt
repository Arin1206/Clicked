package com.example.clicked.view.register

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance()

    private var selectedImageUri: Uri? = null

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

        binding.fileUploadEditTextLayout.setEndIconOnClickListener {
            openGalleryForImage()
        }

        binding.submitregis.setOnClickListener {
            val email = binding.edRegisEmail.text.toString().trim()
            val username = binding.edRegisUsername.text.toString().trim()
            val password = binding.edRegisPassword.text.toString().trim()
            val passwordconfirm = binding.edRegisPasswordconfirm.text.toString().trim()
            val address = binding.edRegisAddress.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || passwordconfirm.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordconfirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Passwords at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        val userId = user?.uid

                        val userMap = hashMapOf(
                            "name" to username,
                            "email" to email,
                            "password" to password,
                            "address" to address
                        )

                        if (userId != null) {
                            db.collection("users").document(userId).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Successfully registered",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    firebaseAuth.signOut()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { exception ->
                                    Log.w(TAG, "Error adding document", exception)
                                }

                            selectedImageUri?.let { uri ->
                                uploadFileToFirebaseStorage(userId, uri)
                            }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.edRegisFileUpload.setText(selectedImageUri.toString())
        }
    }

    private fun uploadFileToFirebaseStorage(userId: String, fileUri: Uri) {
        val storageRef = storage.reference.child("uploads/$userId/${fileUri.lastPathSegment}")
        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val uploadedFileUrl = uri.toString()

                updateUserDataWithFileUrl(userId, uploadedFileUrl)
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "File upload failed: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateUserDataWithFileUrl(userId: String, fileUrl: String) {
        db.collection("users").document(userId)
            .update("fileUrl", fileUrl)
            .addOnSuccessListener {
                Log.d(TAG, "File URL updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error updating file URL", exception)
            }
    }

    fun onButtonLoginClick(view: android.view.View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
