package com.example.kino

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WatchlistActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WatchlistAdapter
    private lateinit var emptyState: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.watchlist_screen)

        recyclerView = findViewById(R.id.rvWatchlist)
        emptyState = findViewById(R.id.emptyState)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Example placeholder list - replace with real data (Room DB or SharedPreferences)
        val movies = mutableListOf(
            Movie("Avatar 2", "2022"),
            Movie("Inception", "2010"),
            Movie("The Batman", "2022"),
            Movie("Oppenheimer", "2023")
        )

        adapter = WatchlistAdapter(movies) { position ->
            // remove clicked item
            movies.removeAt(position)
            adapter.notifyItemRemoved(position)
            toggleEmptyState(movies)
        }
        recyclerView.adapter = adapter

        toggleEmptyState(movies)
    }

    private fun toggleEmptyState(list: List<Movie>) {
        if (list.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}