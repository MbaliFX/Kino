package com.example.kino

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kino.model.MovieResponse


class MovieAdapter(private var movies: List<MovieResponse>, private val onClick: (MovieResponse) -> Unit) :
    RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poster: ImageView = itemView.findViewById(R.id.moviePoster)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        Glide.with(holder.itemView.context)
            .load(movie.Poster)
            .placeholder(R.drawable.placeholder_image) // add a placeholder drawable
            .into(holder.poster)

        holder.itemView.setOnClickListener { onClick(movie) }
    }

    override fun getItemCount(): Int = movies.size

    fun updateMovies(newMovies: List<MovieResponse>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
