package com.example.kino

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kino.model.MovieDetail
import com.example.kino.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class MovieDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        // Find all views from your layout
        val posterImageView: ImageView = findViewById(R.id.detailPosterImageView)
        val titleTextView: TextView = findViewById(R.id.detailTitleTextView)
        val yearTextView: TextView = findViewById(R.id.detailYearTextView)
        val plotTextView: TextView = findViewById(R.id.detailPlotTextView)
        val genreTextView: TextView = findViewById(R.id.detailGenreTextView)
        val directorTextView: TextView = findViewById(R.id.detailDirectorTextView)
        // --- NEW: Find the new TextViews ---
        val typeTextView: TextView = findViewById(R.id.detailTypeTextView)
        val actorsTextView: TextView = findViewById(R.id.detailActorsTextView)

        // Get the data passed from Homescreen
        val movieTitle = intent.getStringExtra("movieTitle")
        val movieYear = intent.getStringExtra("movieYear")
        val moviePoster = intent.getStringExtra("moviePoster")
        val imdbID = intent.getStringExtra("imdbID")

        // Set the initial data that we already have
        titleTextView.text = movieTitle
        yearTextView.text = movieYear
        Glide.with(this).load(moviePoster).into(posterImageView)

        // Fetch detailed information using the imdbID
        if (imdbID != null) {
            fetchMovieDetails(plotTextView, genreTextView, directorTextView, typeTextView, actorsTextView, imdbID)
        } else {
            // Hide fields if no IMDb ID is available (e.g., for TMDb popular movies)
            plotTextView.text = "Details not available for this movie."
            genreTextView.text = ""
            directorTextView.text = ""
            typeTextView.text = ""
            actorsTextView.text = ""
        }
    }

    private fun fetchMovieDetails(
        plotTextView: TextView,
        genreTextView: TextView,
        directorTextView: TextView,
        typeTextView: TextView,
        actorsTextView: TextView,
        imdbId: String
    ) {
        RetrofitInstance.omdbApi.getMovieDetails(imdbId).enqueue(object : Callback<MovieDetail> {
            override fun onResponse(call: Call<MovieDetail>, response: Response<MovieDetail>) {
                if (response.isSuccessful) {
                    val details = response.body()
                    // --- UPDATED: Populate all the text views ---
                    plotTextView.text = details?.Plot ?: "N/A"
                    genreTextView.text = "Genre: ${details?.Genre ?: "N/A"}"
                    directorTextView.text = "Director: ${details?.Director ?: "N/A"}"
                    actorsTextView.text = details?.Actors ?: "N/A"
                    // Capitalize the first letter of the type (e.g., "movie" -> "Movie")
                    typeTextView.text = details?.Type?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    } ?: ""
                } else {
                    Log.e("MovieDetailActivity", "API Error: ${response.code()}")
                    plotTextView.text = "Failed to load details."
                }
            }

            override fun onFailure(call: Call<MovieDetail>, t: Throwable) {
                Log.e("MovieDetailActivity", "Network Failure: ${t.message}")
                plotTextView.text = "Network error. Could not load details."
            }
        })
    }
}
