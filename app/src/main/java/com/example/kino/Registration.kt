package com.example.kino
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class Registration : AppCompatActivity() {

    // Declare Firebase Auth instance
    private lateinit var auth: FirebaseAuth

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Find views by their IDs from activity_registration.xml
        val usernameEditText = findViewById<EditText>(R.id.etUsername)
        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.etConfirmPassword)
        val registerButton = findViewById<Button>(R.id.btnRegister)
        val googleSignInButton = findViewById<Button>(R.id.btnSSO)
        val backToLoginButton = findViewById<TextView>(R.id.btnBackToLogin)

        // Set click listener for the Register button
        registerButton.setOnClickListener {
            // Get text from EditTexts
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Perform validation
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call the function to register the user
            registerUser(email, password)
        }

        // Set click listener for the "Back to Login" button
        backToLoginButton.setOnClickListener {
            val intent = Intent(this, Login::class.java)

            // Add these flags to clear the navigation history
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI and navigate
                    Toast.makeText(baseContext, "Registration successful.", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    // Navigate to the Homescreen
                    val intent = Intent(this, Homescreen::class.java)
                    startActivity(intent)
                    finishAffinity() // Clears the activity stack (Login, Register)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
    }
}
