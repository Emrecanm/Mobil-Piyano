package com.example.denemee

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class giris_sorasi_menu : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var kullaniciAdiSoyadi: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_giris_sorasi_menu)

        // Firebase Authentication instance
        auth = FirebaseAuth.getInstance()

        // Kullanıcı bilgisi için TextView
        kullaniciAdiSoyadi = findViewById(R.id.kullaniciAdiSoyadi)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Kullanıcı mailini al ve TextView'e yaz
        val userEmail = auth.currentUser?.email
        kullaniciAdiSoyadi.text = userEmail ?: "Kullanıcı Bilgisi Bulunamadı"

        // Menu Button Click Listener
        val button = findViewById<ImageButton>(R.id.menuButton2)
        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Log Out Button Click Listener
        val hesabiSilButton = findViewById<Button>(R.id.hesabiSilButton)
        hesabiSilButton.setOnClickListener {
            // Kullanıcıya çıkış yapmayı onaylatan bir dialog göster
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Çıkış Yap")
            builder.setMessage("Çıkış yapmak istediğinizden emin misiniz?")
            builder.setPositiveButton("Evet") { _, _ ->
                // Firebase Authentication - Sign Out
                auth.signOut()

                // Çıkış yaptıktan sonra kullanıcıyı login ekranına yönlendir
                Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
                finish() // Bu Activity'yi kapat
            }
            builder.setNegativeButton("Hayır") { dialog, _ ->
                // Kullanıcı Hayır'a bastığında dialog kapanır
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Kullanıcı mailini tekrar güncelle
        val userEmail = auth.currentUser?.email
        kullaniciAdiSoyadi.text = userEmail ?: "Kullanıcı Bilgisi Bulunamadı"
    }
}
