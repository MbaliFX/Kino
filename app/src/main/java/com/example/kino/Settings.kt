package com.example.kino

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class Settings : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUser: FirebaseUser? = null

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

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // --- View References ---
        val textFullName = findViewById<TextView>(R.id.textFullName) // Registered name (non-editable)
        val textUsername = findViewById<TextView>(R.id.textUsername) // Current username
        val textEmail = findViewById<TextView>(R.id.textEmail)       // Current email

        val editUsername = findViewById<EditText>(R.id.editUsername)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)

        val btnUpdateUserInfo = findViewById<Button>(R.id.btnUpdateUserInfo)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)
        val switchNotifications = findViewById<Switch>(R.id.switchNotifications)
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        //val btnSave = findViewById<Button>(R.id.btnSave)
        val btnClearData = findViewById<Button>(R.id.btnClearData)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // --- Load User Info from Firestore ---
        loadUserInfo(textFullName, textUsername, textEmail, editUsername, editEmail)

        // --- Update User Info ---
        btnUpdateUserInfo.setOnClickListener {
            val newUsername = editUsername.text.toString().trim()
            val newEmail = editEmail.text.toString().trim()
            updateUserInfo(newUsername, newEmail)
        }

        // --- Change Password ---
        btnChangePassword.setOnClickListener {
            val newPassword = editPassword.text.toString().trim()
            if (newPassword.isNotEmpty()) {
                currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        editPassword.text.clear()
                    } else {
                        Toast.makeText(this, "Password change failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Enter a new password", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Dark Mode Toggle ---
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // --- Notifications Toggle ---
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Notifications enabled" else "Notifications disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // --- Language Selection ---
        val languages = arrayOf("English", "Zulu", "Afrikaans", "French")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        // --- Clear Data ---
        btnClearData.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "All data cleared and logged out!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // --- Logout ---
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadUserInfo(
        textFullName: TextView,
        textUsername: TextView,
        textEmail: TextView,
        editUsername: EditText,
        editEmail: EditText
    ) {
        currentUser?.let { user ->
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fullName = document.getString("fullName") ?: ""
                        val username = document.getString("username") ?: ""
                        val email = user.email ?: ""

                        textFullName.text = fullName
                        textUsername.text = username
                        textEmail.text = email

                        editUsername.setText(username)
                        editEmail.setText(email)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load user info: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun updateUserInfo(newUsername: String, newEmail: String) {
        val userDoc = firestore.collection("users").document(currentUser!!.uid)
        val updates = mutableMapOf<String, Any>()
        if (newUsername.isNotEmpty()) updates["username"] = newUsername

        userDoc.update(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update username: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Update email in Firebase Auth
        if (newEmail.isNotEmpty() && newEmail != currentUser!!.email) {
            currentUser!!.updateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentUser!!.sendEmailVerification()
                    Toast.makeText(
                        this,
                        "Email updated. Verification email sent to $newEmail",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this, "Email update failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
