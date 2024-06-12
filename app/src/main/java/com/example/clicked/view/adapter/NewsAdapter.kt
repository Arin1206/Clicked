package com.example.clicked.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clicked.R
import com.example.clicked.data.News

class NewsAdapter(private var newsList: List<News>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_posts, parent, false)
        return NewsViewHolder(view)
    }


    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.bind(newsItem)
    }

    fun updateNewsList(newsList: List<News>) {
        this.newsList = newsList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgItemPhoto = itemView.findViewById<ImageView>(R.id.img_item_photo)
        private val judulPosts = itemView.findViewById<TextView>(R.id.judulposts)
        private val tanggalPosts = itemView.findViewById<TextView>(R.id.tanggalposts)
        private val isiPosts = itemView.findViewById<TextView>(R.id.isiposts)

        fun bind(newsItem: News) {
            judulPosts.text = newsItem.title
            tanggalPosts.text = newsItem.date
            isiPosts.text = newsItem.paragraph

            // Load image using Glide if imageUrl is not null
            newsItem.imageUrl?.let {
                Glide.with(itemView.context)
                    .load(it)
                    .into(imgItemPhoto)
            }
        }
    }
}
