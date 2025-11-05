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

    // --- NEW: State variables for infinite scrolling ---
    private var currentPage = 1
    private var isFetching = false // Prevents multiple requests at the same time
    private var isSearchMode = false // Tracks if we are showing popular movies or search results

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homescreen)

        // Initialize Views
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.movieRecyclerView)
        searchView = findViewById(R.id.search_view)
        logoButton = findViewById(R.id.logo_button)

        setSupportActionBar(toolbar)
        setupRecyclerView()
        setupSearchView()
        setupLogoButton()

        val searchQuery = intent.getStringExtra("SEARCH_QUERY")
        if (!searchQuery.isNullOrBlank()) {
            searchView.setQuery(searchQuery, true)
        } else {
            loadPopularMovies(currentPage) // Load the first page
        }
    }

    // ... (onCreateOptionsMenu and onOptionsItemSelected are correct) ...
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_homescreen, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, Settings::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun setupLogoButton() {
        logoButton.setOnClickListener {
            val intent = Intent(this, SearchScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        // We use a lambda to pass the onClick action to the adapter.
        adapter = MovieAdapter(emptyList()) { movie ->
            val intent = Intent(this, MovieDetailActivity::class.java).apply {
                putExtra("movieTitle", movie.title)
                putExtra("movieYear", movie.year)
                putExtra("moviePoster", movie.poster)
                // Pass imdbID if it's available. OMDB search has it, TMDb popular does not.
                if (movie.imdbID != null) {
                    putExtra("imdbID", movie.imdbID)
                } else {
                    putExtra("tmdbID", movie.tmdbId)
                }
            }
            startActivity(intent)
        }

        val layoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        // --- NEW: Add the scroll listener for infinite scrolling ---
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Only load more if not in search mode and not already fetching
                if (isSearchMode || isFetching) return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Check if we've scrolled to the end of the list
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    currentPage++
                    loadPopularMovies(currentPage)
                }
            }
        })
    }

    private fun setupSearchView() {
        val searchCloseIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchCloseIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.search_icon))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                if (!query.isNullOrBlank()) {
                    isSearchMode = true // Enter search mode
                    searchOmdbMovies(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    isSearchMode = false // Exit search mode
                    currentPage = 1 // Reset to page 1 for popular movies
                    loadPopularMovies(currentPage)
                }
                return true
            }
        })
    }

    // --- UPDATED: Data Loading Methods ---

    private fun loadPopularMovies(page: Int) {
        if (isFetching) return
        isFetching = true

        val tmdbApiKey = "c8e980b95ae1bea670c408a20e65c4b4"

        RetrofitInstance.tmdbApi.getPopularMovies(
            apiKey = tmdbApiKey,
            page = page
        ).enqueue(object : Callback<TMDbResponse> {
            override fun onResponse(call: Call<TMDbResponse>, response: Response<TMDbResponse>) {
                if (response.isSuccessful) {
                    val tmdbMovies = response.body()?.results ?: emptyList()
                    val movies = tmdbMovies.map { tmdbMovie ->
                        Movie(
                            title = tmdbMovie.title,
                            year = tmdbMovie.releaseDate.substringBefore("-"),
                            imdbID = null,
                            poster = tmdbMovie.getFullPosterUrl(),
                            tmdbId = tmdbMovie.id
                        )
                    }
                    if (page == 1) {
                        adapter.updateMovies(movies)
                    } else {
                        adapter.addMovies(movies)
                    }
                } else {
                    Log.e("KinoAPI", "TMDb API Error: ${response.code()}")
                }
                isFetching = false
            }

            override fun onFailure(call: Call<TMDbResponse>, t: Throwable) {
                Log.e("KinoAPI", "TMDb Network Failure: ${t.message}")
                isFetching = false
            }
        })
    }


    private fun searchOmdbMovies(query: String) {
        RetrofitInstance.omdbApi.searchMovies(query).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful && response.body()?.Response == "True") {
                    val movies = response.body()?.Search ?: emptyList()
                    adapter.updateMovies(movies) // Search results replace the current list
                } else {
                    Log.e("KinoAPI", "OMDB Search Error: ${response.body()?.Error}")
                    adapter.updateMovies(emptyList())
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                Log.e("KinoAPI", "OMDB Network Failure: ${t.message}")
            }
        })
    }
}
