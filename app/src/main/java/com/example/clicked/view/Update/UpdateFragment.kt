package com.example.clicked.view.Update

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.clicked.R
import com.example.clicked.databinding.FragmentUpdateBinding
import com.example.clicked.view.common.BaseFragment
import com.example.clicked.view.main.MainActivity
import com.example.clicked.view.profile.ProfileFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class UpdateFragment : BaseFragment<FragmentUpdateBinding>(FragmentUpdateBinding::inflate) {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0
    private val REQUEST_IMAGE_GALLERY = 102
    private var currentImagePath: String? = null
    private var currentImageUri: Uri? = null
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var newsId: String
    private lateinit var spinnerItems: Array<String>
    override fun setupUI() {
        arguments?.let {
            newsId = it.getString("newsId") ?: ""
            fetchNewsDetails(newsId)
        }
        spinnerItems = resources.getStringArray(R.array.spinner_items)

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.mySpinneredit.adapter = adapter
    }


    override fun setupListeners() {
        binding.saveedit.setOnClickListener {
            saveNewsDetails()
        }
        binding.camera.setOnClickListener {
            startCamera()
        }


        binding.gallery.setOnClickListener {
            openGallery()
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri()
        currentImageUri?.let {
            launcherIntentCamera.launch(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    it
                )
            )
        }
    }

    private fun getImageUri(): Uri {
        val timestamp = System.currentTimeMillis() / 1000
        val imageFileName = "JPEG_" + timestamp.toString() + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentImagePath = image.absolutePath
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            image
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            imageUri?.let {
                Log.d(ContentValues.TAG, "Image Uri from gallery: $it")
                binding.imagepreviewedit.setImageURI(it)
                currentImagePath = it.toString()
            } ?: run {
                Log.d(ContentValues.TAG, "Image Uri is null")
            }
        }
    }

    private fun openGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupListeners()


        getLocation()
        (activity as? MainActivity)?.binding?.bottomnavigation?.visibility = View.GONE

    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as? MainActivity)?.binding?.bottomnavigation?.visibility = View.VISIBLE
    }

    private fun fetchNewsDetails(newsId: String) {
        val db = FirebaseFirestore.getInstance()
        val newsRef = db.collection("news").document(newsId)

        newsRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {

                binding.judulberitaedit.setText(document.getString("judulBerita"))
                binding.paragraf1edit.setText(document.getString("paragraf1"))
                binding.paragraf2edit.setText(document.getString("paragraf2"))
                binding.paragraf3edit.setText(document.getString("paragraf3"))


                val spinnerItem = document.getString("spinnerItem") ?: ""
                val spinnerPosition = spinnerItems.indexOf(spinnerItem)
                if (spinnerPosition != -1) {
                    binding.mySpinneredit.setSelection(spinnerPosition)
                }


                val includePosition = document.getBoolean("includePosition") ?: false
                binding.includePositionCheckBoxedit.isChecked = includePosition

                val imageUrl = document.getString("imageUrl") ?: ""

                Glide.with(this).load(imageUrl).into(binding.imagepreviewedit)
            }
        }.addOnFailureListener { exception ->

        }
    }

    private fun saveNewsDetails() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        val newsRef = db.collection("news").document(newsId)

        val includePosition = binding.includePositionCheckBoxedit.isChecked
        val latitude = if (includePosition) currentLat else null
        val longitude = if (includePosition) currentLon else null

        val dateFormat = SimpleDateFormat("d MMMM yyyy")
        val currentDate = dateFormat.format(Date())

        val userId = auth.currentUser?.uid ?: ""


        newsRef.get().addOnSuccessListener { document ->
            val existingImageUrl = document.getString("imageUrl") ?: ""

            val imageUrl = currentImageUri?.toString() ?: existingImageUrl

            if (imageUrl.isEmpty()) {
                binding.loadingProgressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Image cannot be empty", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val updatedNews = mutableMapOf(
                "judulBerita" to binding.judulberitaedit.text.toString(),
                "paragraf1" to binding.paragraf1edit.text.toString(),
                "paragraf2" to binding.paragraf2edit.text.toString(),
                "paragraf3" to binding.paragraf3edit.text.toString(),
                "spinnerItem" to binding.mySpinneredit.selectedItem.toString(),
                "includePosition" to includePosition,
                "latitude" to latitude,
                "longitude" to longitude,
                "formattedDate" to currentDate,
                "timestamp" to FieldValue.serverTimestamp(),
                "imageUrl" to imageUrl,
                "userId" to userId
            )

            if (currentImageUri != null) {

                val storageRef =
                    FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}_image.jpg")
                val uploadTask = storageRef.putFile(currentImageUri!!)

                uploadTask.addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val newImageUrl = uri.toString()
                        updatedNews["imageUrl"] = newImageUrl


                        newsRef.set(updatedNews).addOnSuccessListener {
                            binding.loadingProgressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "News details saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            val profileFragment = ProfileFragment()
                            activity?.supportFragmentManager?.beginTransaction()
                                ?.replace(R.id.frame, profileFragment)
                                ?.addToBackStack(null)?.commit()
                        }.addOnFailureListener { exception ->
                            binding.loadingProgressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Failed to save news details",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.addOnFailureListener { exception ->
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {

                newsRef.set(updatedNews).addOnSuccessListener {
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "News details saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val profileFragment = ProfileFragment()
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.frame, profileFragment)
                        ?.addToBackStack(null)?.commit()
                }.addOnFailureListener { exception ->
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Failed to save news details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.addOnFailureListener { exception ->
            binding.loadingProgressBar.visibility = View.GONE
            Toast.makeText(
                requireContext(),
                "Failed to fetch existing image URL",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private val launcherIntentCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentImageUri?.let { uri ->
                    Log.d(ContentValues.TAG, "Image Uri from camera: $uri")
                    binding.imagepreviewedit.setImageURI(uri)
                } ?: run {
                    Log.d(ContentValues.TAG, "Image Uri is null")
                }
            }
        }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                UpdateFragment.REQUEST_LOCATION_PERMISSION
            )
        } else {
            Log.d(ContentValues.TAG, "Permission already granted, getting location")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLat = it.latitude
                        currentLon = it.longitude
                        Toast.makeText(
                            requireContext(),
                            "Latitude: $currentLat, Longitude: $currentLon",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(ContentValues.TAG, "Latitude: $currentLat, Longitude: $currentLon")
                    } ?: run {
                        Toast.makeText(
                            requireContext(),
                            "Unable to retrieve location",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(ContentValues.TAG, "Location is null")
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error getting location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(ContentValues.TAG, "Error getting location: ${e.message}")
                }
        }
    }

    private fun showImage() {
        currentImageUri?.let { uri ->
            binding.imagepreviewedit.setImageURI(uri)
        }
    }

    override fun setupObservers() {


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
    }
}
