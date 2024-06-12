package com.example.clicked.view.upload

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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.example.clicked.R
import com.example.clicked.databinding.FragmentUploadBinding
import com.example.clicked.view.common.BaseFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class UploadFragment : BaseFragment<FragmentUploadBinding>(FragmentUploadBinding::inflate) {
    private val REQUEST_IMAGE_CAPTURE = 101
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0
    private val REQUEST_IMAGE_GALLERY = 102
    private var currentImagePath: String? = null
    private var currentImageUri: Uri? = null
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val launcherIntentCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentImageUri?.let { uri ->
                Log.d(ContentValues.TAG, "Image Uri from camera: $uri")
                binding.imagepreview.setImageURI(uri)
            } ?: run {
                Log.d(ContentValues.TAG, "Image Uri is null")
            }
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            Log.d(ContentValues.TAG, "Permission already granted, getting location")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLat = it.latitude
                        currentLon = it.longitude
                        Toast.makeText(requireContext(), "Latitude: $currentLat, Longitude: $currentLon", Toast.LENGTH_SHORT).show()
                        Log.d(ContentValues.TAG, "Latitude: $currentLat, Longitude: $currentLon")
                    } ?: run {
                        Toast.makeText(requireContext(), "Unable to retrieve location", Toast.LENGTH_SHORT).show()
                        Log.d(ContentValues.TAG, "Location is null")
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.d(ContentValues.TAG, "Error getting location: ${e.message}")
                }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupListeners()

        // Request user location when the fragment opens
        getLocation()
    }

    private fun startCamera() {
        currentImageUri = getImageUri()
        currentImageUri?.let {
            launcherIntentCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, it))
        }
    }

    private fun getImageUri(): Uri {
        val timestamp = System.currentTimeMillis() / 1000
        val imageFileName = "JPEG_" + timestamp.toString() + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentImagePath = image.absolutePath
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", image)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            imageUri?.let {
                Log.d(ContentValues.TAG, "Image Uri from gallery: $it")
                binding.imagepreview.setImageURI(it)
                currentImagePath = it.toString()
            } ?: run {
                Log.d(ContentValues.TAG, "Image Uri is null")
            }
        }
    }

    override fun setupUI() {
        // Initialize the Spinner
        val mySpinner = binding.mySpinner

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_items,
            android.R.layout.simple_spinner_item
        )

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        mySpinner.adapter = adapter
    }

    override fun setupListeners() {
        // Setup listener for image preview click
        binding.camera.setOnClickListener {
            startCamera()
        }

        // Setup listener for gallery button
        binding.gallery.setOnClickListener {
            openGallery()
        }

        // Setup listener for save button
        binding.saveButton.setOnClickListener {
            saveData()
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
            showImage() // Panggil fungsi showImage() di sini untuk menampilkan gambar yang dipilih
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let { uri ->
            binding.imagepreview.setImageURI(uri)
        }
    }

    private fun saveData() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        val judulBerita = binding.judulberita.text.toString().trim()
        val paragraf1 = binding.paragraf1.text.toString().trim()
        val paragraf2 = binding.paragraf2.text.toString().trim()
        val paragraf3 = binding.paragraf3.text.toString().trim()
        val spinnerItem = binding.mySpinner.selectedItem.toString()
        val includePosition = binding.includePositionCheckBox.isChecked
        val currentTimeMillis = System.currentTimeMillis() // Mendapatkan waktu saat ini

        // Validate required fields
        if (currentImageUri == null || judulBerita.isEmpty() || paragraf1.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Upload image to Firebase Storage
        currentImageUri?.let { uri ->
            val storageRef = FirebaseStorage.getInstance().reference.child("images/${uri.lastPathSegment}")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val formattedDate = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(currentTimeMillis)
                            saveToFirestore(userId, downloadUrl.toString(), judulBerita, paragraf1, paragraf2, paragraf3, spinnerItem, includePosition, currentTimeMillis) // Menyimpan waktu saat ini bersama data berita
                        } else {
                            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.d(ContentValues.TAG, "Error uploading image: ${e.message}")
                }
        }
    }


    private fun saveToFirestore(userId: String, imageUrl: String, judulBerita: String, paragraf1: String, paragraf2: String, paragraf3: String, spinnerItem: String, includePosition: Boolean, currentTimeMillis: Long) {
        binding.loadingProgressBar.visibility = View.VISIBLE
        val formattedDate = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(currentTimeMillis)

        val newsData = hashMapOf(
            "userId" to userId,
            "imageUrl" to imageUrl,
            "judulBerita" to judulBerita,
            "paragraf1" to paragraf1,
            "paragraf2" to paragraf2,
            "paragraf3" to paragraf3,
            "spinnerItem" to spinnerItem,
            "includePosition" to includePosition,
            "timestamp" to currentTimeMillis, // Menambahkan waktu saat ini ke data berita
            "formattedDate" to formattedDate // Menambahkan tanggal yang diformat ke data berita
        )

        if (includePosition) {
            newsData["latitude"] = currentLat
            newsData["longitude"] = currentLon
        }

        FirebaseFirestore.getInstance().collection("news")
            .add(newsData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "News saved successfully", Toast.LENGTH_SHORT).show()
                Log.d(ContentValues.TAG, "News saved successfully")
                binding.loadingProgressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error saving news: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d(ContentValues.TAG, "Error saving news: ${e.message}")
                binding.loadingProgressBar.visibility = View.GONE
            }
    }



    override fun setupObservers() {
// Set up any observers here
    }
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == REQUEST_LOCATION_PERMISSION) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                } else {
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

        companion object {
            private const val REQUEST_LOCATION_PERMISSION = 100
        }
    }
