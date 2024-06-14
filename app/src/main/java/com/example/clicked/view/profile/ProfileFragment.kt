package com.example.clicked.view.profile

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clicked.data.News
import com.example.clicked.databinding.FragmentProfileBinding
import com.example.clicked.view.adapter.NewsAdapter
import com.example.clicked.view.common.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate)  {
    private lateinit var adapter: NewsAdapter

    override fun setupUI() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val recyclerView = binding.rvPost
        adapter = NewsAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


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
            fetchNewsFromFirestore(userId)
            fetchDataFromFirebase()
        }
    }

    private fun fetchDataFromFirebase() {
        binding.loadingProgressBar.visibility = View.VISIBLE

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
                    val imageUrl = document.getString("fileUrl")


                    binding.nameprof.text = name
                    binding.emailprof.text = email
                    binding.alamatprof.text = address


                    imageUrl?.let {
                        Glide.with(requireContext())
                            .load(it)
                            .into(binding.imageprofile2)
                    }


                    val totalPosts = document.getLong("totalPosts") ?: 0
                    binding.totalpost.text = totalPosts.toString()

                    binding.loadingProgressBar.visibility = View.GONE
                } else {
                    Log.d(ContentValues.TAG, "Document does not exist")
                    binding.loadingProgressBar.visibility = View.GONE
                }
            }.addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting document: $exception")
                binding.loadingProgressBar.visibility = View.GONE
            }
        }
    }

    private fun countPosts(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val newsRef = db.collection("news")
            .whereEqualTo("userId", userId)

        newsRef.get().addOnSuccessListener { documents ->
            val totalPosts = documents.size()
            binding.totalpost.text = totalPosts.toString()
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "Error counting posts: $exception")
        }
    }

    private fun fetchNewsFromFirestore(userId: String) {

        binding.loadingProgressBar.visibility = View.VISIBLE


        val db = FirebaseFirestore.getInstance()
        val newsRef = db.collection("news")
            .whereEqualTo("userId", userId)

        newsRef.get().addOnSuccessListener { documents ->
            val newsList = mutableListOf<News>()

            for (document in documents) {
                val id = document.id
                val title = document.getString("judulBerita")
                val imageUrl = document.getString("imageUrl")
                val date = document.getString("formattedDate")
                val paragraph = document.getString("paragraf1")


                val newsItem = News(id,title, imageUrl, date, paragraph)
                newsList.add(newsItem)
            }


            adapter.updateNewsList(newsList)


            countPosts(userId)


            binding.loadingProgressBar.visibility = View.GONE
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "Error getting documents: $exception")

            binding.loadingProgressBar.visibility = View.GONE
        }
    }

    private fun deleteNewsFromFirestore(news: News) {
        val db = FirebaseFirestore.getInstance()
        val newsRef = db.collection("news")


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
