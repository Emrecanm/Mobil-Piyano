package com.example.denemee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MenuActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Menu Button Click Listener
        val button = findViewById<ImageButton>(R.id.menuButton2)
        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Sign In Button Click Listener
        val button2 = findViewById<Button>(R.id.singInButton)
        button2.setOnClickListener {
            val intent = Intent(this, sign_in::class.java)
            startActivity(intent)
        }

        // Log In Button Click Listener
        val button3 = findViewById<Button>(R.id.logInButton)
        button3.setOnClickListener {
            val intent = Intent(this, log_in::class.java)
            startActivity(intent)
        }
    }
}
