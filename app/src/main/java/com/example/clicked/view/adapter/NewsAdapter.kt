package com.example.clicked.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clicked.R
import com.example.clicked.data.News
import com.example.clicked.view.Update.UpdateFragment
import com.example.clicked.view.detail.DetailFragment

class NewsAdapter(private var newsList: List<News>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    var onClickListener: View.OnClickListener? = null
    private lateinit var activity: FragmentActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_posts, parent, false)
        activity = parent.context as FragmentActivity
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.bind(newsItem)
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("newsId", newsItem.id)
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

    fun updateNewsList(newsList: List<News>) {
        this.newsList = newsList
        notifyDataSetChanged()
    }

    fun getNewsAtPosition(position: Int): News {
        return newsList[position]
    }

    fun removeNewsAtPosition(position: Int) {
        val mutableList = newsList.toMutableList()
        mutableList.removeAt(position)
        newsList = mutableList
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgItemPhoto = itemView.findViewById<ImageView>(R.id.img_item_photo)
        private val judulPosts = itemView.findViewById<TextView>(R.id.judulposts)
        private val tanggalPosts = itemView.findViewById<TextView>(R.id.tanggalposts)
        private val isiPosts = itemView.findViewById<TextView>(R.id.isiposts)
        private val editButton = itemView.findViewById<TextView>(R.id.buttonedit)


        fun bind(newsItem: News) {
            judulPosts.text = newsItem.title
            tanggalPosts.text = newsItem.date
            isiPosts.text = newsItem.paragraph

            newsItem.imageUrl?.let {
                Glide.with(itemView.context)
                    .load(it)
                    .into(imgItemPhoto)
            }
            editButton.tag = newsItem.id
            editButton.setOnClickListener {
                val newsId = it.tag as String

                val bundle = Bundle().apply {
                    putString("newsId", newsId)
                }
                val updateFragment = UpdateFragment().apply {
                    arguments = bundle
                }
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame, updateFragment)
                    .addToBackStack(null)
                    .commit()


            }
        }
    }


}
