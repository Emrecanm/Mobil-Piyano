package com.example.denemee

import SongAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaPlayer

class SarkiListesi : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sarki_listesi)

        // WindowInsets ayarları
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // Geri butonunu bulma ve tıklama olayı
        val backButton = findViewById<ImageButton>(R.id.back1Button3)
        backButton?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Şarkı listesi RecyclerView ve adaptör
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Şarkı listesini tanımla
        val songList = mutableListOf("KARGA", "ŞARKI 2", "ŞARKI 3", "ŞARKI 4", "ŞARKI 5")

        // Adaptörü bağla
        recyclerView.adapter = SongAdapter(
            songList,
            onFavoriteClick = { song, message ->
                Toast.makeText(this, "$song $message", Toast.LENGTH_SHORT).show()
            },
            onListenClick = { song ->
                // Eğer şarkı "KARGA" ise MediaPlayer ile çal
                if (song == "KARGA") {
                    mediaPlayer = MediaPlayer.create(this, R.raw.karga)  // karga.mp3 dosyasını çal
                    mediaPlayer.start()
                    Toast.makeText(this, "$song çalıyor!", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { song ->
                // Şarkı silme işleminde alert dialog ekleyebilirsiniz, burada basitçe silme işlemi yapıyoruz
                songList.remove(song)  // Şarkıyı listeden sil
                recyclerView.adapter?.notifyDataSetChanged()  // RecyclerView'ı güncelle
                Toast.makeText(this, "$song silindi!", Toast.LENGTH_SHORT).show()
            },
            onPlayClick = { song ->
                // ButtonPlay'e tıklandığında SesAnaliziActivity'ye yönlendiriyoruz
                val intent = Intent(this, ses_analizi::class.java)
                intent.putExtra("songName", song)  // Şarkı adını geçiyoruz
                startActivity(intent)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Activity kapanırken MediaPlayer'ı serbest bırak
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
