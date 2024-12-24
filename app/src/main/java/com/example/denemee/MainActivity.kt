package com.example.denemee

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Window insets için ayarlar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Menü tuşu
        val button = findViewById<ImageButton>(R.id.menuButton)
        button.setOnClickListener {
            // Kullanıcının giriş durumu kontrol edilir
            if (auth.currentUser != null) {
                // Kullanıcı giriş yapmışsa MenuActivity'e yönlendir
                val intent = Intent(this, giris_sorasi_menu::class.java)
                startActivity(intent)
            } else {
                // Kullanıcı giriş yapmamışsa log_in ekranına yönlendir
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
            }
        }

        // Serbest Çal tuşu
        val button2 = findViewById<ImageButton>(R.id.serbestCalbutton)
        button2.setOnClickListener {
            val intent = Intent(this, SerbestCalActivity::class.java)
            startActivity(intent)
        }

        // Şarkı Listesi tuşu
        val button3 = findViewById<ImageButton>(R.id.sarkiListesibutton)
        button3.setOnClickListener {
            val intent = Intent(this, SarkiListesi::class.java)
            startActivity(intent)
        }

        // Favoriler tuşu
        val button4 = findViewById<ImageButton>(R.id.favorilerbutton)
        button4.setOnClickListener {
            val intent = Intent(this, Favoriler::class.java)
            startActivity(intent)
        }

        // Ses Analizi tuşu
        val button5 = findViewById<ImageButton>(R.id.analizButton)
        button5.setOnClickListener {
            val intent = Intent(this, ses_analizi::class.java)
            startActivity(intent)
        }
    }
}
