package com.example.clicked.view.profile

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clicked.R
import com.example.clicked.data.News
import com.example.clicked.databinding.FragmentProfileBinding
import com.example.clicked.view.adapter.NewsAdapter
import com.example.clicked.view.common.BaseFragment
import com.example.clicked.view.welcome.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate)  {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: NewsAdapter

    override fun setupUI() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        val recyclerView = binding.rvPost
        adapter = NewsAdapter(emptyList()) // Initialize with an empty list
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Attach ItemTouchHelper to RecyclerView
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val newsItem = adapter.getNewsAtPosition(position)

                // Show confirmation dialog
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete News")
                    .setMessage("Are you sure you want to delete this news?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        deleteNewsFromFirestore(newsItem)
                        adapter.removeNewsAtPosition(position)
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        adapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun setupListeners() {

    }

    override fun setupObservers() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            fetchNewsFromFirestore(userId) // Call the function with user ID
            fetchDataFromFirebase() // Call the function to fetch user data
        }
    }

    private fun fetchDataFromFirebase() {
        binding.loadingProgressBar.visibility = View.VISIBLE // Show ProgressBar
        // Mengambil data dari Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)
            userRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    val email = document.getString("email")
                    val address = document.getString("address")
                    val imageUrl = document.getString("fileUrl") // Ambil URL gambar dari Firestore

                    // Tampilkan data di UI
                    binding.nameprof.text = name
                    binding.emailprof.text = email
                    binding.alamatprof.text = address

                    // Tampilkan gambar di ImageView jika URL gambar tersedia
                    imageUrl?.let {
                        Glide.with(requireContext())
                            .load(it)
                            .into(binding.imageprofile2)
                    }

                    // Hitung dan tampilkan jumlah news yang dipost
                    val totalPosts = document.getLong("totalPosts") ?: 0
                    binding.totalpost.text = totalPosts.toString() // Bind to totalposts TextView

                    binding.loadingProgressBar.visibility = View.GONE // Hide ProgressBar after loading data
                } else {
                    Log.d(ContentValues.TAG, "Document does not exist")
                    binding.loadingProgressBar.visibility = View.GONE // Hide ProgressBar if document does not exist
                }
            }.addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting document: $exception")
                binding.loadingProgressBar.visibility = View.GONE // Hide ProgressBar on failure
            }
        }
    }

    private fun countPosts(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val newsRef = db.collection("news")
            .whereEqualTo("userId", userId) // Filter news by user ID

        newsRef.get().addOnSuccessListener { documents ->
            val totalPosts = documents.size() // Count the number of news posts
            binding.totalpost.text = totalPosts.toString() // Bind to totalposts TextView
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "Error counting posts: $exception")
        }
    }

    private fun fetchNewsFromFirestore(userId: String) {
        // Show ProgressBar
        binding.loadingProgressBar.visibility = View.VISIBLE

        // Fetch news data from Firestore based on user ID
        val db = FirebaseFirestore.getInstance()
        val newsRef = db.collection("news")
            .whereEqualTo("userId", userId) // Filter news by user ID

        newsRef.get().addOnSuccessListener { documents ->
            val newsList = mutableListOf<News>() // Create a list to hold news items

            for (document in documents) {
                val id = document.id
                val title = document.getString("judulBerita")
                val imageUrl = document.getString("imageUrl")
                val date = document.getString("formattedDate")
                val paragraph = document.getString("paragraf1")

                // Add news item to the list
                val newsItem = News(id,title, imageUrl, date, paragraph)
                newsList.add(newsItem)
            }

            // Update RecyclerView adapter with the fetched data
            adapter.updateNewsList(newsList)

            // Count and display the total number of posts
            countPosts(userId)

            // Hide ProgressBar after loading data
            binding.loadingProgressBar.visibility = View.GONE
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "Error getting documents: $exception")
            // Hide ProgressBar on failure
            binding.loadingProgressBar.visibility = View.GONE
        }
    }

    private fun deleteNewsFromFirestore(news: News) {
        val db = FirebaseFirestore.getInstance()
        val newsRef = db.collection("news")

        // Assuming "title" is a unique identifier for news items
        newsRef.whereEqualTo("judulBerita", news.title).get().addOnSuccessListener { documents ->
            for (document in documents) {
                newsRef.document(document.id).delete().addOnSuccessListener {
                    Log.d(ContentValues.TAG, "News successfully deleted")
                }.addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error deleting news", e)
                }
            }
        }
    }
}
