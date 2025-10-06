package com.example.kino

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.etEmailForgotPassword)
        val sendLinkButton = findViewById<Button>(R.id.btnSendResetLink)

        sendLinkButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendPasswordResetEmail(email)
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Password reset email sent. Please check your inbox.", Toast.LENGTH_LONG).show()
                    // Finish this activity and go back to the Login screen
                    finish()
                } else {
                    Toast.makeText(baseContext, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
