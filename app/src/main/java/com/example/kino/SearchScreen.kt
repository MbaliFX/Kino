package com.example.kino

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SearchScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() is often not needed when setting window insets manually.
        setContentView(R.layout.activity_search_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- NEW: Make the logo clickable ---
        val logoButton: ImageView = findViewById(R.id.logo)
        logoButton.setOnClickListener {
            // Navigate to the Homescreen when the logo is clicked.
            val intent = Intent(this, Homescreen::class.java)
            startActivity(intent)
        }

        // The search logic will be handled on the Homescreen, so you can add
        // the code here to navigate when a search is submitted.
        val searchView: androidx.appcompat.widget.SearchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    val intent = Intent(this@SearchScreen, Homescreen::class.java).apply {
                        // Pass the search query to the Homescreen
                        putExtra("SEARCH_QUERY", query)
                    }
                    startActivity(intent)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }
}
