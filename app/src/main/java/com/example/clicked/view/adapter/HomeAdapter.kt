package com.example.clicked.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.clicked.R
import com.example.clicked.data.Post
import com.example.clicked.view.detail.DetailFragment

class HomeAdapter(private var posts: List<Post>, private val activity: FragmentActivity) : RecyclerView.Adapter<HomeAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val judulPosts = itemView.findViewById<TextView>(R.id.judulposts)
        private val tanggalPosts = itemView.findViewById<TextView>(R.id.tanggalposts)
        private val cardview = itemView.findViewById<LinearLayout>(R.id.cardview)

        fun bind(post: Post, activity: FragmentActivity, postId: String) {
            judulPosts.text = post.title
            tanggalPosts.text = post.date

            itemView.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("newsId", post.id)
                }
                val detailFragment = DetailFragment().apply {
                    arguments = bundle
                }
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_posts_home, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post, activity, post.id)
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
