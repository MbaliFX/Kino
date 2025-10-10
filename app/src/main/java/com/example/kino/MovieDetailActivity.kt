package com.example.kino

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kino.model.MovieDetail // This import will now work correctly
import com.example.kino.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        val posterImageView: ImageView = findViewById(R.id.detailPosterImageView)
        val titleTextView: TextView = findViewById(R.id.detailTitleTextView)
        val yearTextView: TextView = findViewById(R.id.detailYearTextView)
        val plotTextView: TextView = findViewById(R.id.detailPlotTextView)
        val genreTextView: TextView = findViewById(R.id.detailGenreTextView)
        val directorTextView: TextView = findViewById(R.id.detailDirectorTextView)

        // --- Get data passed from Homescreen ---
        val movieTitle = intent.getStringExtra("movieTitle")
        val movieYear = intent.getStringExtra("movieYear")
        val moviePoster = intent.getStringExtra("moviePoster")
        val imdbID = intent.getStringExtra("imdbID")

        // --- Set the data we already have ---
        titleTextView.text = movieTitle
        yearTextView.text = movieYear
        Glide.with(this)
            .load(moviePoster)
            .placeholder(R.drawable.placeholder_image)
            .into(posterImageView)

        // --- Fetch the rest of the details using the imdbID ---
        if (imdbID != null) {
            // If we have an ID, fetch the details from the network
            fetchMovieDetails(imdbID, plotTextView, genreTextView, directorTextView)
        } else {
            // If no ID was passed, we can't fetch more details
            plotTextView.text = "Plot details not available."
            genreTextView.text = ""
            directorTextView.text = ""
        }
    }

    private fun fetchMovieDetails(imdbId: String, plotTextView: TextView, genreTextView: TextView, directorTextView: TextView) {
        RetrofitInstance.omdbApi.getMovieDetails(imdbId).enqueue(object : Callback<MovieDetail> {
            override fun onResponse(call: Call<MovieDetail>, response: Response<MovieDetail>) {
                if (response.isSuccessful) {
                    val details = response.body()
                    // Update the UI with the new, detailed information
                    plotTextView.text = details?.Plot ?: "N/A"
                    genreTextView.text = "Genre: ${details?.Genre ?: "N/A"}"
                    directorTextView.text = "Director: ${details?.Director ?: "N/A"}"
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
