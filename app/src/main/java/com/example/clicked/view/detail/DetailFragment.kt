package com.example.clicked.view.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.example.clicked.databinding.FragmentDetailBinding
import com.example.clicked.view.common.BaseFragment
import com.example.clicked.view.main.MainActivity
import com.google.firebase.firestore.FirebaseFirestore

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

        val shareText = "Check out this news:\n$title\nDate: $date\n\n$paragraph1"

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
            firestore.collection("news").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userId = document.getString("userId")
                        if (userId != null) {
                            firestore.collection("users").document(userId).get()
                                .addOnSuccessListener { userDocument ->
                                    if (userDocument != null && userDocument.exists()) {
                                        val username = userDocument.getString("name")

                                        binding.nameprofile.text = username
                                        val imageUrl2 =
                                            userDocument.getString("fileUrl") // Ubah menjadi fileUrl sesuai dengan dokumen Anda

                                        imageUrl2?.let {
                                            Glide.with(this)
                                                .load(it)
                                                .into(binding.imageprofile)

                                        }

                                    } else {

                                    }
                                }
                                .addOnFailureListener { e ->
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

                    }
                    binding.floatingProgressBar.visibility = View.GONE
                }
                .addOnFailureListener { e ->

                    binding.floatingProgressBar.visibility = View.GONE
                }
        }
    }


    override fun setupListeners() {

    }

    override fun setupObservers() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.binding?.bottomnavigation?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as? MainActivity)?.binding?.bottomnavigation?.visibility = View.VISIBLE
    }


}
