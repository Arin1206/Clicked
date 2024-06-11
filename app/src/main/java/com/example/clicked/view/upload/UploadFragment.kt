package com.example.clicked.view.upload

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import com.example.clicked.R
import com.example.clicked.databinding.FragmentUploadBinding
import com.example.clicked.view.common.BaseFragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File

class UploadFragment : BaseFragment<FragmentUploadBinding>(FragmentUploadBinding::inflate) {
    private val REQUEST_IMAGE_CAPTURE = 101
    private val REQUEST_IMAGE_GALLERY = 102
    private var currentImagePath: String? = null
    private var currentImageUri: Uri? = null
    private val launcherIntentCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap?
            imageBitmap?.let {
                binding.imagepreview.setImageBitmap(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
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
            binding.imagepreview.setImageURI(imageUri)
            currentImagePath = imageUri.toString()
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
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY)
    }

    override fun setupObservers() {
        // Set up any observers here
    }
}
