package com.example.denemee

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import java.io.IOException

class SarkiListesi : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var favoriteSongs: MutableList<String>
    private var mediaPlayer1: MediaPlayer? = null
    private var songUri: Uri? = null
    var dosyaadi="dosya"

    private fun playSong(uri: Uri) {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(this, uri)
            mediaPlayer.prepare()
            mediaPlayer.start()
            val intent = Intent(this, ses_analizi::class.java)
            intent.putExtra("songName", dosyaadi)
            startActivity(intent)
        } catch (e: IOException) {
            // Hata durumunda kullanıcıya bildirim
            Toast.makeText(this, "Şarkı çalınamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sarki_listesi)
        val vSongName = findViewById<TextView>(R.id.tv_song_name)
        val btnPlaySong = findViewById<Button>(R.id.btn_play_song)
        val songName = intent.getStringExtra("SONG_NAME") ?: "Eklenen şarkı yok"
        val uriString = intent.getStringExtra("SONG_URI") ?: "content://media/external/audio/media/1" // Fallback URI
       val  tvSongName = findViewById<TextView>(R.id.tv_song_name)
       var varbtnPlaySong = findViewById<Button>(R.id.btn_play_song)
        val songUri = Uri.parse(uriString) // Geçerli bir URI olduğunda bunu parse et

        tvSongName.text = songName
        dosyaadi=songName
        btnPlaySong.setOnClickListener {
            playSong(songUri)
        }



        // WindowInsets ayarları
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // SharedPreferences'i başlat
        sharedPreferences = getSharedPreferences("FavoriteSongs", MODE_PRIVATE)
        favoriteSongs = mutableListOf(*sharedPreferences.getStringSet("favorites", setOf())?.toTypedArray() ?: emptyArray())

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
        val songList = mutableListOf("KARGA", "KARGA2", "KARGA3", "ŞARKI 4", "ŞARKI 5")

        // Adaptörü bağla
        recyclerView.adapter = SongAdapter(
            songList,
            onFavoriteClick = { song, message ->
                if (favoriteSongs.contains(song)) {
                    favoriteSongs.remove(song)
                    Toast.makeText(this, "$song favorilerden kaldırıldı!", Toast.LENGTH_SHORT).show()
                } else {
                    favoriteSongs.add(song)
                    Toast.makeText(this, "$song favorilere eklendi!", Toast.LENGTH_SHORT).show()
                }

                // SharedPreferences'e favori şarkıları kaydet
                sharedPreferences.edit().putStringSet("favorites", favoriteSongs.toSet()).apply()
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
                songList.remove(song)
                recyclerView.adapter?.notifyDataSetChanged()
                Toast.makeText(this, "$song silindi!", Toast.LENGTH_SHORT).show()
            },
            onPlayClick = { song ->
                val intent = Intent(this, ses_analizi::class.java)
                intent.putExtra("songName", song)
                startActivity(intent)
            },
            sharedPreferences = sharedPreferences // SharedPreferences'i buradan gearing
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
