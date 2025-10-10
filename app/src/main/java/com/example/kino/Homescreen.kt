package com.example.kino

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kino.model.Movie
import com.example.kino.model.MovieDetail
import com.example.kino.model.MovieResponse
import com.example.kino.model.TMDbResponse
import com.example.kino.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Homescreen : AppCompatActivity() {

    private lateinit var adapter: MovieAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var toolbar: Toolbar
    private lateinit var logoButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homescreen)

        // Initialize Views
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.movieRecyclerView)
        searchView = findViewById(R.id.search_view)
        logoButton = findViewById(R.id.logo_button)

        // Setup Toolbar
        setSupportActionBar(toolbar)

        // Setup other components
        setupRecyclerView()
        setupSearchView()
        setupLogoButton()

        // Check if we received a search query from another screen
        val searchQuery = intent.getStringExtra("SEARCH_QUERY")
        if (!searchQuery.isNullOrBlank()) {
            searchView.setQuery(searchQuery, true) // Set query and submit
        } else {
            // Otherwise, load popular movies automatically
            loadPopularMovies()
        }
    }

    // --- Toolbar and Menu Methods ---

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar.
        menuInflater.inflate(R.menu.menu_homescreen, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item clicks
        return when (item.itemId) {
            R.id.action_settings -> {
                // Navigate to Settings activity
                val intent = Intent(this, Settings::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // --- Setup Methods ---

    private fun setupLogoButton() {
        logoButton.setOnClickListener {
            // Navigate back to the SearchScreen
            val intent = Intent(this, SearchScreen::class.java)
            // Clear the activity stack so the user doesn't get stuck in a loop
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Close the Homescreen
        }
    }

    private fun setupRecyclerView() {
        adapter = MovieAdapter(emptyList()) { movie ->
            val intent = Intent(this, MovieDetailActivity::class.java).apply {
                putExtra("movieTitle", movie.title)
                putExtra("movieYear", movie.year)
                putExtra("moviePoster", movie.poster)
                putExtra("imdbID", movie.imdbID)
            }
            startActivity(intent)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        // Replace the default close icon with the search icon
        val searchCloseIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchCloseIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.search_icon))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus() // Hide keyboard
                if (!query.isNullOrBlank()) {
                    searchOmdbMovies(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // When text is cleared, show popular movies again
                if (newText.isNullOrBlank()) {
                    loadPopularMovies()
                }
                return true
            }
        })
    }

    // --- Data Loading Methods ---

    private fun loadPopularMovies() {
        RetrofitInstance.tmdbApi.getPopularMovies().enqueue(object : Callback<TMDbResponse> {
            override fun onResponse(call: Call<TMDbResponse>, response: Response<TMDbResponse>) {
                if (response.isSuccessful) {
                    val tmdbMovies = response.body()?.results ?: emptyList()
                    val movies = tmdbMovies.map { tmdbMovie ->
                        Movie(
                            title = tmdbMovie.title,
                            year = tmdbMovie.releaseDate.substringBefore("-"),
                            imdbID = null,
                            poster = tmdbMovie.getFullPosterUrl()
                        )
                    }
                    adapter.updateMovies(movies)
                } else {
                    Log.e("KinoAPI", "TMDb API Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TMDbResponse>, t: Throwable) {
                Log.e("KinoAPI", "TMDb Network Failure: ${t.message}")
            }
        })
    }

    // In Homescreen.kt
// ...
    private fun searchOmdbMovies(query: String) {
        // CORRECTED: The Callback now correctly uses MovieResponse
        RetrofitInstance.omdbApi.searchMovies(query).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful && response.body()?.Response == "True") {
                    // This now works because MovieResponse has a 'Search' property
                    val movies = response.body()?.Search ?: emptyList()
                    adapter.updateMovies(movies)
                } else {
                    Log.e("KinoAPI", "OMDB Search Error: ${response.body()?.Error}")
                    adapter.updateMovies(emptyList()) // Clear list if no results
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                Log.e("KinoAPI", "OMDB Network Failure: ${t.message}")
            }
        })
    }
//...

}
