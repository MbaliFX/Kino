package com.example.kino

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Settings : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    @SuppressLint("SetTextI18n", "UseSwitchCompatOrMaterialCode", "UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // SharedPreferences setup
        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // --- Welcome TextView ---
        val txtWelcome = findViewById<TextView>(R.id.txtWelcome)
        val savedName = prefs.getString("username", "")
        if (!savedName.isNullOrEmpty()) {
            txtWelcome.text = "Welcome, $savedName!"
        } else {
            txtWelcome.text = "Welcome!"
        }

        // --- 1. DARK MODE TOGGLE ---
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)
        switchDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
        }

        // --- 2. CHANGE USERNAME ---
        val editUsername = findViewById<EditText>(R.id.editUsername)
        val btnSaveUsername = findViewById<Button>(R.id.btnSaveUsername)
        editUsername.setText(savedName)
        btnSaveUsername.setOnClickListener {
            val newName = editUsername.text.toString().trim()
            if (newName.isNotEmpty()) {
                prefs.edit().putString("username", newName).apply()
                txtWelcome.text = "Welcome, $newName!"
                Toast.makeText(this, "Username saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 3. LOGOUT BUTTON ---
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }

        // --- 4. NOTIFICATIONS TOGGLE ---
        val switchNotifications = findViewById<Switch>(R.id.switchNotifications)
        switchNotifications.isChecked = prefs.getBoolean("notifications", true)
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications", isChecked).apply()
            val message = if (isChecked) "Notifications enabled" else "Notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // --- 5. LANGUAGE SELECTION ---
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val languages = arrayOf("English", "Zulu", "Afrikaans", "French")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        val savedLang = prefs.getString("language", "English")
        spinnerLanguage.setSelection(languages.indexOf(savedLang))
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedLang = languages[position]
                prefs.edit().putString("language", selectedLang).apply()
                Toast.makeText(
                    this@Settings,
                    "Language set to $selectedLang",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // --- 6. CLEAR DATA BUTTON ---
        val btnClearData = findViewById<Button>(R.id.btnClearData)
        btnClearData.setOnClickListener {
            prefs.edit().clear().apply()
            Toast.makeText(this, "All data cleared!", Toast.LENGTH_SHORT).show()
            editUsername.setText("")
            txtWelcome.text = "Welcome!"
            switchDarkMode.isChecked = false
            switchNotifications.isChecked = true
            spinnerLanguage.setSelection(0)
        }
    }
}

