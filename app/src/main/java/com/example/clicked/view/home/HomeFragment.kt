package com.example.clicked.view.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clicked.R
import com.example.clicked.data.Post
import com.example.clicked.databinding.FragmentHomeBinding
import com.example.clicked.view.adapter.HomeAdapter
import com.example.clicked.view.common.BaseFragment
import com.example.clicked.view.detail.DetailFragment
import com.example.clicked.view.main.MainActivity
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private lateinit var firestore: FirebaseFirestore

    override fun setupUI() {
        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize RecyclerViews
        setupRecyclerView(binding.rvPostTeknologi, "Teknologi")
        setupRecyclerView(binding.rvPostBisnis, "Bisnis")
        setupRecyclerView(binding.rvPostKesehatan, "Kesehatan")
        setupRecyclerView(binding.rvPostEkonomi, "Ekonomi")
        setupRecyclerView(binding.rvPostLifestyle, "Lifestyle")
        setupRecyclerView(binding.rvPostPolitik, "Politik")


        fetchLatestPosts()
        binding.share.setOnClickListener {
            shareNews()
        }
    }
    private fun shareNews() {
        val title = binding.textbanner.text.toString()

        // Tambahkan paragraf lainnya jika diperlukan

        val shareText = "Check out this news:\n$title\n"
        // Tambahkan paragraf lainnya ke dalam shareText jika diperlukan

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }


    override fun setupListeners() {
        // Implement any listeners if necessary
    }

    override fun setupObservers() {
        // Implement any observers if necessary
    }


    private fun setupRecyclerView(recyclerView: RecyclerView, category: String) {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.loadingProgressBar.visibility = View.VISIBLE
        recyclerView.adapter = HomeAdapter(emptyList(), requireActivity()).apply {
            fetchPosts(category)

        }
    }

    private fun HomeAdapter.fetchPosts(category: String) {
        firestore.collection("news")
            .whereEqualTo("spinnerItem", category)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map { document ->
                    Post(
                        id = document.id,
                        title = document.getString("judulBerita") ?: "",
                        date = document.getString("formattedDate") ?: ""
                    )
                }
                // Log the number of fetched documents
                Log.d("HomeFragment", "Fetched ${posts.size} documents for category $category and $id")
                updatePosts(posts)

                // Check if posts are empty and update visibility of empty text view
                if (posts.isEmpty()) {
                    when (category) {
                        "Teknologi" -> binding.textkosong.visibility = View.VISIBLE
                        "Bisnis" -> binding.textkosong2.visibility = View.VISIBLE
                        "Kesehatan" -> binding.textkosong3.visibility = View.VISIBLE
                        "Ekonomi" -> binding.textkosong4.visibility = View.VISIBLE
                        "Lifestyle" -> binding.textkosong5.visibility = View.VISIBLE
                        "Politik" -> binding.textkosong6.visibility = View.VISIBLE
                    }
                }
                binding.loadingProgressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.e("HomeFragment", "Error fetching documents: ", exception)
                binding.loadingProgressBar.visibility = View.GONE
            }
    }
    private fun fetchLatestPosts() {
        // Assuming you have a field named "timestamps" in your Firestore documents
        firestore.collection("news")
            .orderBy("timestamps", Query.Direction.DESCENDING) // Order by timestamps in descending order to get the latest posts first
            .limit(1) // Limit to 1 post to get the latest post
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val latestPostDocument = documents.first() // Get the first document which is the latest post
                    val imageUrl = latestPostDocument.getString("imageUrl") // Replace "imageUrl" with the field name containing the image URL
                    val title = latestPostDocument.getString("judulBerita") ?: "" // Replace "judulBerita" with the field name containing the post title
                    val postId = latestPostDocument.id // Get the document ID

                    // Bind the data to ImageView and TextView
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .into(binding.image) // Replace "imageView" with your ImageView's ID in your layout
                    binding.textbanner.text = title // Replace "textViewTitle" with your TextView's ID in your layout

                    // Set onClickListener for cardViewLayout
                    binding.cardview.setOnClickListener {
                        val bundle = Bundle().apply {
                            putString("newsId", postId) // Pass postId to DetailFragment
                        }
                        val detailFragment = DetailFragment().apply {
                            arguments = bundle
                        }
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.frame, detailFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                } else {
                    // Handle case when there are no posts
                    Log.d("HomeFragment", "No posts found.")
                }
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.e("HomeFragment", "Error fetching latest post: ", exception)
            }
    }



}
