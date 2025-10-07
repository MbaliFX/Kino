package com.example.kino

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kino.model.MovieResponse
import com.example.kino.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java

class Homescreen : AppCompatActivity() {

    private lateinit var adapter: MovieAdapter
    private val trendingTitles = listOf("Inception", "The Matrix", "Avatar", "Interstellar", "Titanic", "Avengers")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homescreen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.movieRecyclerView)
        adapter = MovieAdapter(emptyList()) { movie ->
            // Open movie details
            val intent = Intent(this, MovieDetailActivity::class.java)
            intent.putExtra("movieTitle", movie.Title)
            intent.putExtra("movieYear", movie.Year)
            intent.putExtra("moviePoster", movie.Poster)
            intent.putExtra("moviePlot", movie.Plot)
            startActivity(intent)
        }

        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 posters per row
        recyclerView.adapter = adapter

        // Load trending movies automatically
        loadTrendingMovies()

        // Search functionality
        val searchView: SearchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchMovie(it) }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun loadTrendingMovies() {
        val movies = mutableListOf<MovieResponse>()
        trendingTitles.forEach { title ->
            RetrofitInstance.api.getMovieByTitle(title).enqueue(object : Callback<MovieResponse> {
                override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                    response.body()?.let { movie ->
                        movies.add(movie)
                        adapter.updateMovies(movies)
                    }
                }
                override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                    Log.e("KinoAPI", "Error loading trending: ${t.message}")
                }
            })
        }
    }

    private fun searchMovie(query: String) {
        RetrofitInstance.api.getMovieByTitle(query).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                response.body()?.let { movie ->
                    // Replace the current list with the search result
                    adapter.updateMovies(listOf(movie))
                }
            }
            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                Log.e("KinoAPI", "Search Error: ${t.message}")
            }
        })
    }
}
