package com.example.kino

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kino.model.MovieDetail
import com.example.kino.model.TMDbVideoResponse
import com.example.kino.model.TMDbMovieDetail
import com.example.kino.model.TmdbCreditsResponse
import com.example.kino.model.TmdbFindResponse
import com.example.kino.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var trailerButton: Button
    private lateinit var trailerContainer: FrameLayout
    private lateinit var webView: WebView

    private val tmdbApiKey = "c8e980b95ae1bea670c408a20e65c4b4"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

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

        val movieTitle = intent.getStringExtra("movieTitle")
        val movieYear = intent.getStringExtra("movieYear")
        val moviePoster = intent.getStringExtra("moviePoster")
        var imdbID = intent.getStringExtra("imdbID")
        val tmdbID = intent.getIntExtra("tmdbID", -1)

        titleTextView.text = movieTitle
        yearTextView.text = movieYear
        Glide.with(this).load(moviePoster).into(posterImageView)

        if (imdbID != null) {
            fetchOmdbDetails(imdbID, plotTextView, genreTextView, directorTextView, typeTextView, actorsTextView)
            fetchTmdbDetails(imdbID, charactersTextView)
            fetchTrailer(tmdbID)
        } else if (tmdbID != -1) {
            fetchImdbIdFromTmdb(tmdbID) { fetchedImdbId ->
                if (fetchedImdbId != null) {
                    imdbID = fetchedImdbId
                    fetchOmdbDetails(fetchedImdbId, plotTextView, genreTextView, directorTextView, typeTextView, actorsTextView)
                    fetchTmdbDetails(fetchedImdbId, charactersTextView)
                }
            }
            fetchTrailer(tmdbID)
        } else {
            plotTextView.text = "Details not available."
        }

        trailerButton.setOnClickListener {
            trailerContainer.visibility = View.VISIBLE
            trailerButton.visibility = View.GONE
        }

        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = object : WebChromeClient() {}
    }

    private fun fetchImdbIdFromTmdb(tmdbId: Int, callback: (String?) -> Unit) {
        RetrofitInstance.tmdbApi.getMovieDetails(tmdbId, tmdbApiKey).enqueue(object : Callback<TMDbMovieDetail> {
            override fun onResponse(call: Call<TMDbMovieDetail>, response: Response<TMDbMovieDetail>) {
                if (response.isSuccessful) {
                    callback(response.body()?.imdbId)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<TMDbMovieDetail>, t: Throwable) {
                callback(null)
            }
        })
    }

    private fun fetchOmdbDetails(
        imdbId: String,
        plotTextView: TextView,
        genreTextView: TextView,
        directorTextView: TextView,
        typeTextView: TextView,
        actorsTextView: TextView
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
                } else {
                    Log.e("MovieDetailActivity", "OMDb API Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MovieDetail>, t: Throwable) {
                Log.e("MovieDetailActivity", "OMDb Failure: ${t.message}")
            }
        })
    }

    private fun fetchTmdbDetails(imdbId: String, charactersTextView: TextView) {
        RetrofitInstance.tmdbApi.findByImdbId(imdbId, tmdbApiKey).enqueue(object : Callback<TmdbFindResponse> {
            override fun onResponse(call: Call<TmdbFindResponse>, response: Response<TmdbFindResponse>) {
                val movieResult = response.body()?.movie_results?.firstOrNull()
                if (movieResult != null) {
                    fetchCharacterDetails(movieResult.id, charactersTextView)
                } else {
                    charactersTextView.text = "Characters not available."
                }
            }

            override fun onFailure(call: Call<TmdbFindResponse>, t: Throwable) {
                Log.e("MovieDetailActivity", "TMDb Find Failure: ${t.message}")
            }
        })
    }

    private fun fetchCharacterDetails(tmdbId: Int, charactersTextView: TextView) {
        RetrofitInstance.tmdbApi.getMovieCredits(tmdbId, tmdbApiKey).enqueue(object : Callback<TmdbCreditsResponse> {
            override fun onResponse(call: Call<TmdbCreditsResponse>, response: Response<TmdbCreditsResponse>) {
                if (response.isSuccessful) {
                    val castList = response.body()?.cast?.take(5)?.joinToString("\n") {
                        "${it.name} as ${it.character}"
                    }
                    charactersTextView.text = castList ?: "Characters not available."
                } else {
                    charactersTextView.text = "Failed to load characters."
                }
            }

            override fun onFailure(call: Call<TmdbCreditsResponse>, t: Throwable) {
                Log.e("MovieDetailActivity", "Credits Failure: ${t.message}")
            }
        })
    }

    private fun fetchTrailer(tmdbId: Int) {
        RetrofitInstance.tmdbApi.getMovieVideos(tmdbId, tmdbApiKey).enqueue(object : Callback<TMDbVideoResponse> {
            override fun onResponse(call: Call<TMDbVideoResponse>, response: Response<TMDbVideoResponse>) {
                if (response.isSuccessful) {
                    val trailer = response.body()?.results?.find { it.type == "Trailer" }
                    if (trailer != null) {
                        val videoId = trailer.key
                        val embedUrl = "https://www.youtube.com/embed/$videoId"
                        webView.loadUrl(embedUrl)
                        trailerButton.visibility = View.VISIBLE
                    } else {
                        trailerButton.visibility = View.GONE
                        trailerContainer.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<TMDbVideoResponse>, t: Throwable) {
                Log.e("MovieDetailActivity", "Trailer Failure: ${t.message}")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}
