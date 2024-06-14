package com.example.clicked.view.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import com.example.clicked.R
import com.example.clicked.databinding.FragmentDetailBinding
import com.example.clicked.view.common.BaseFragment
import com.example.clicked.view.main.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class DetailFragment : BaseFragment<FragmentDetailBinding>(FragmentDetailBinding::inflate) {

    private lateinit var firestore: FirebaseFirestore
    private var newsId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        arguments?.let {
            newsId = it.getString("newsId")
        }
    }

    private fun shareNews() {
        val title = binding.judulnews.text.toString()
        val date = binding.tanggalposts.text.toString()
        val paragraph1 = binding.paragrafdetail1.text.toString()
        // Tambahkan paragraf lainnya jika diperlukan

        val shareText = "Check out this news:\n$title\nDate: $date\n\n$paragraph1"
        // Tambahkan paragraf lainnya ke dalam shareText jika diperlukan

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
    override fun setupUI() {
        binding.floatingProgressBar.visibility = View.VISIBLE
        binding.share.setOnClickListener {
            shareNews()
        }
        newsId?.let { id ->
            // Mengambil data dari koleksi "news" berdasarkan ID
            firestore.collection("news").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userId = document.getString("userId")
                        // Mengambil data dari koleksi "users" berdasarkan user ID
                        if (userId != null) {
                            firestore.collection("users").document(userId).get()
                                .addOnSuccessListener { userDocument ->
                                    if (userDocument != null && userDocument.exists()) {
                                        val username = userDocument.getString("name")
                                        // Mengikat data ke ID XML

                                        binding.nameprofile.text = username
                                        val imageUrl2 = userDocument.getString("fileUrl") // Ubah menjadi fileUrl sesuai dengan dokumen Anda

                                        imageUrl2?.let {
                                            Glide.with(this)
                                                .load(it)
                                                .into(binding.imageprofile)

                                        }

                                    } else {
                                        // Handle the case where the user document is not found
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Handle any errors when fetching user data
                                }
                        }

                        val title = document.getString("judulBerita")
                        val date = document.getString("formattedDate")
                        val paragraph = document.getString("paragraf1")
                        val paragraph2 = document.getString("paragraf2")
                        val paragraph3 = document.getString("paragraf3")
                        val imageUrl = document.getString("imageUrl")

                        binding.judulnews.text = title
                        binding.tanggalposts.text = date
                        binding.paragrafdetail1.text = paragraph
                        binding.paragrafdetail2.text = paragraph2
                        binding.paragrafdetail3.text = paragraph3

                        imageUrl?.let {
                            Glide.with(this)
                                .load(it)
                                .into(binding.image)

                        }
                    } else {
                        // Handle the case where the document is not found
                    }
                    binding.floatingProgressBar.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    // Handle any errors when fetching news data
                    binding.floatingProgressBar.visibility = View.GONE
                }
        }
    }


    override fun setupListeners() {
        // Implement any required listeners
    }

    override fun setupObservers() {
        // Implement any required observers
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Hide bottom navigation
        (activity as? MainActivity)?.binding?.bottomnavigation?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show bottom navigation when leaving DetailFragment
        (activity as? MainActivity)?.binding?.bottomnavigation?.visibility = View.VISIBLE
    }


}
