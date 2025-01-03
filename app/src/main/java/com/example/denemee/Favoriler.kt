package com.example.denemee

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Favoriler : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var favoriteSongs: MutableList<String> // Favoriler MutableList olmalı, çünkü öğeleri değiştireceğiz
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoriler)

        // WindowInsets ayarları
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // SharedPreferences'i başlat
        sharedPreferences = getSharedPreferences("FavoriteSongs", MODE_PRIVATE)

        // Favori şarkıları SharedPreferences'ten al
        favoriteSongs = sharedPreferences.getStringSet("favorites", setOf())?.toMutableList() ?: mutableListOf()

        // Favori şarkıları göstermek için RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Adaptörü bağla
        recyclerView.adapter = SongAdapter(
            favoriteSongs,
            onFavoriteClick = { song, message ->
                // Favori şarkılar güncelleniyor
                if (!favoriteSongs.contains(song)) {
                    favoriteSongs.add(song)  // Favoriye ekle
                    saveFavorites()  // Favorileri kaydet
                    Toast.makeText(this, "$song favorilere eklendi!", Toast.LENGTH_SHORT).show()
                } else {
                    favoriteSongs.remove(song)  // Favoriden çıkar
                    saveFavorites()  // Favorileri kaydet
                    Toast.makeText(this, "$song favorilerden kaldırıldı!", Toast.LENGTH_SHORT).show()
                }
            },
            onListenClick = { song ->
                // Şarkıya göre MediaPlayer oluştur ve çal
                val mediaResId = when (song) {
                    "KARGA" -> R.raw.karga
                    "KARGA2" -> R.raw.kargahatali
                    "KARGA3" -> R.raw.kargahatali2
                    else -> null
                }

                if (mediaResId != null) {
                    mediaPlayer = MediaPlayer.create(this, mediaResId)
                    mediaPlayer.start()
                    Toast.makeText(this, "$song çalıyor!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "$song için bir medya kaynağı bulunamadı!", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { song ->
                favoriteSongs.remove(song) // Şarkıyı listeden sil
                saveFavorites() // Favorileri güncelle
                recyclerView.adapter?.notifyDataSetChanged() // RecyclerView'ı güncelle
            },
            onPlayClick = { song ->
                // ButtonPlay'e tıklandığında SesAnaliziActivity'ye yönlendiriyoruz
                val intent = Intent(this, ses_analizi::class.java)
                intent.putExtra("songName", song)  // Şarkı adını geçiyoruz
                startActivity(intent)
            },
            sharedPreferences = sharedPreferences // SharedPreferences'i buradan gearing

        )

        // Geri butonunu bulma ve tıklama olayı
        val backButton = findViewById<ImageButton>(R.id.back1Button2)
        backButton.setOnClickListener {
            finish() // Favoriler sayfasını kapat
        }
    }

    // Favori şarkıları SharedPreferences'e kaydetme fonksiyonu
    private fun saveFavorites() {
        sharedPreferences.edit()
            .putStringSet("favorites", favoriteSongs.toSet()) // Set olarak kaydediyoruz
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Activity kapanırken MediaPlayer'ı serbest bırak
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
