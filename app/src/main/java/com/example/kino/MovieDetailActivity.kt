package com.example.kino

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kino.model.MovieResponse
import com.example.kino.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        val poster: ImageView = findViewById(R.id.detailPoster)
        val title: TextView = findViewById(R.id.detailTitle)
        val year: TextView = findViewById(R.id.detailYear)
        val plot: TextView = findViewById(R.id.detailPlot)

        val movieTitle = intent.getStringExtra("MOVIE_TITLE") ?: return

        RetrofitInstance.api.getMovieByTitle(movieTitle).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                response.body()?.let { movie ->
                    title.text = movie.Title
                    year.text = movie.Year
                    plot.text = movie.Plot
                    Glide.with(this@MovieDetailActivity)
                        .load(movie.Poster)
                        .placeholder(R.drawable.placeholder_image)
                        .into(poster)
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                Log.e("KinoAPI", "Detail Error: ${t.message}")
            }
        })
    }
}
