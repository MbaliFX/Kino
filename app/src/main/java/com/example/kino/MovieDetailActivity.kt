package com.example.kino

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kino.model.MovieDetail
import com.example.kino.model.TmdbFindResponse
import com.example.kino.model.TmdbCreditsResponse
import com.example.kino.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import android.webkit.WebView
import android.webkit.WebViewClient

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var trailerButton: Button
    private lateinit var trailerContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        // View references
        val posterImageView: ImageView = findViewById(R.id.detailPosterImageView)
        val titleTextView: TextView = findViewById(R.id.detailTitleTextView)
        val yearTextView: TextView = findViewById(R.id.detailYearTextView)
        val plotTextView: TextView = findViewById(R.id.detailPlotTextView)
        val genreTextView: TextView = findViewById(R.id.detailGenreTextView)
        val directorTextView: TextView = findViewById(R.id.detailDirectorTextView)
        val typeTextView: TextView = findViewById(R.id.detailTypeTextView)
        val actorsTextView: TextView = findViewById(R.id.detailActorsTextView)
        val charactersTextView: TextView = findViewById(R.id.detailCharactersTextView)
        trailerButton = findViewById(R.id.trailerButton)
        trailerContainer = findViewById(R.id.trailerContainer)

        // Get intent extras
        val movieTitle = intent.getStringExtra("movieTitle")
        val movieYear = intent.getStringExtra("movieYear")
        val moviePoster = intent.getStringExtra("moviePoster")
        val imdbID = intent.getStringExtra("imdbID")
        val trailerYouTubeId = intent.getStringExtra("trailerYouTubeId") // pass YouTube ID if available

        // Set initial UI values
        titleTextView.text = movieTitle
        yearTextView.text = movieYear
        Glide.with(this).load(moviePoster).into(posterImageView)

        // Setup trailer button click
        trailerButton.setOnClickListener {
            trailerButton.visibility = View.GONE
            trailerContainer.visibility = View.VISIBLE
            if (!trailerYouTubeId.isNullOrEmpty()) {
                showYouTubeTrailer(trailerYouTubeId)
            } else {
                // Show a placeholder or error if no trailer available
                val placeholder = TextView(this).apply {
                    text = "Trailer not available"
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                }
                trailerContainer.addView(placeholder)
            }
        }

        if (imdbID != null) {
            fetchMovieDetails(
                plotTextView,
                genreTextView,
                directorTextView,
                typeTextView,
                actorsTextView,
                charactersTextView,
                imdbID
            )
        } else {
            plotTextView.text = "Details not available."
            genreTextView.text = ""
            directorTextView.text = ""
            typeTextView.text = ""
            actorsTextView.text = ""
        }
    }

    private fun showYouTubeTrailer(videoId: String) {
        // Using a WebView to embed YouTube trailer
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        val html = """
            <html>
            <body style="margin:0;padding:0;">
            <iframe width="100%" height="100%" 
                src="https://www.youtube.com/embed/$videoId?autoplay=1" 
                frameborder="0" allowfullscreen>
            </iframe>
            </body>
            </html>
        """.trimIndent()
        webView.loadData(html, "text/html", "utf-8")
        trailerContainer.addView(webView)
    }

    private fun fetchMovieDetails(
        plotTextView: TextView,
        genreTextView: TextView,
        directorTextView: TextView,
        typeTextView: TextView,
        actorsTextView: TextView,
        charactersTextView: TextView,
        imdbId: String
    ) {
        RetrofitInstance.omdbApi.getMovieDetails(imdbId).enqueue(object : Callback<MovieDetail> {
            override fun onResponse(call: Call<MovieDetail>, response: Response<MovieDetail>) {
                if (response.isSuccessful) {
                    val details = response.body()
                    plotTextView.text = details?.Plot ?: "N/A"
                    genreTextView.text = "Genre: ${details?.Genre ?: "N/A"}"
                    directorTextView.text = "Director: ${details?.Director ?: "N/A"}"
                    actorsTextView.text = details?.Actors ?: "N/A"
                    typeTextView.text = details?.Type?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    } ?: "N/A"

                    // Fetch character names from TMDb
                    fetchCharacterDetails(imdbId, charactersTextView)
                } else {
                    Log.e("MovieDetailActivity", "OMDb API Error: ${response.code()}")
                    plotTextView.text = "Failed to load details."
                }
            }

            override fun onFailure(call: Call<MovieDetail>, t: Throwable) {
                Log.e("MovieDetailActivity", "OMDb Failure: ${t.message}")
                plotTextView.text = "Network error. Could not load details."
            }
        })
    }

    private fun fetchCharacterDetails(imdbId: String, charactersTextView: TextView) {
        val apiKey = "TMDB_API_KEY" // Replace or use BuildConfig.TMDB_API_KEY

        RetrofitInstance.tmdbApi.findByImdbId(imdbId, apiKey).enqueue(object : Callback<TmdbFindResponse> {
            override fun onResponse(call: Call<TmdbFindResponse>, response: Response<TmdbFindResponse>) {
                val movie = response.body()?.movie_results?.firstOrNull()
                if (movie != null) {
                    val tmdbId = movie.id
                    RetrofitInstance.tmdbApi.getMovieCredits(tmdbId, apiKey)
                        .enqueue(object : Callback<TmdbCreditsResponse> {
                            override fun onResponse(call: Call<TmdbCreditsResponse>, response: Response<TmdbCreditsResponse>) {
                                val castList = response.body()?.cast?.take(5)?.joinToString("\n") {
                                    "${it.name} as ${it.character}"
                                }
                                charactersTextView.text = castList ?: "Characters not available."
                            }

                            override fun onFailure(call: Call<TmdbCreditsResponse>, t: Throwable) {
                                Log.e("MovieDetailActivity", "Credits fetch failed: ${t.message}")
                                charactersTextView.text = "Failed to load characters."
                            }
                        })
                } else {
                    charactersTextView.text = "TMDb movie not found."
                }
            }

            override fun onFailure(call: Call<TmdbFindResponse>, t: Throwable) {
                Log.e("MovieDetailActivity", "TMDb find failed: ${t.message}")
                charactersTextView.text = "Could not connect to TMDb."
            }
        })
    }
}
