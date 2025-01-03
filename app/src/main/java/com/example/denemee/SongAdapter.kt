package com.example.denemee

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

// Favori durumlarını SharedPreferences'e kaydetmek ve yüklemek için değişiklikler
class SongAdapter(
    private val songs: MutableList<String>,
    private val onFavoriteClick: (String, String) -> Unit,
    private val onPlayClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onListenClick: (String) -> Unit,
    private val sharedPreferences: SharedPreferences // SharedPreferences'i adapter'e gönderiyoruz
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    // Favori durumu için map oluşturuyoruz ve SharedPreferences'ten yüklüyoruz
    private val favoriteStates: MutableMap<String, Boolean> = loadFavorites()

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songName: TextView = view.findViewById(R.id.textSongName)
        val buttonFavorite: ImageButton = view.findViewById(R.id.buttonFavorite)
        val buttonListen: ImageButton = view.findViewById(R.id.buttonListen)
        val buttonDelete: ImageButton = view.findViewById(R.id.buttonDelete)
        val buttonPlay: ImageButton = view.findViewById(R.id.buttonPlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        // Favori durumunu kontrol et
        val isFavorite = favoriteStates[song] ?: false
        holder.buttonFavorite.setImageResource(
            if (isFavorite) R.drawable.bookmark2 else R.drawable.bookmark
        )

        holder.songName.text = song

        holder.buttonFavorite.setOnClickListener {
            val newState = !(favoriteStates[song] ?: false)
            favoriteStates[song] = newState

            // Favori durumunu güncelle ve kaydet
            saveFavorites()

            holder.buttonFavorite.setImageResource(
                if (newState) R.drawable.bookmark2 else R.drawable.bookmark
            )

            if (newState) {
                onFavoriteClick(song, "Favorilere eklendi!")
            } else {
                onFavoriteClick(song, "Favorilerden kaldırıldı!")
            }
        }

        holder.buttonListen.setOnClickListener { onListenClick(song) }

        holder.buttonDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Silme İşlemi")
                .setMessage("$song şarkısını silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                    songs.removeAt(position)
                    favoriteStates.remove(song) // Favori listesinden de kaldır
                    saveFavorites() // Güncellenen durumu kaydet
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, songs.size)
                    onDeleteClick(song)
                }
                .setNegativeButton("Hayır", null)
                .show()
        }

        holder.buttonPlay.setOnClickListener { onPlayClick(song) }
    }

    override fun getItemCount(): Int = songs.size

    // Favorileri SharedPreferences'e kaydet
    private fun saveFavorites() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("favorites", favoriteStates.filter { it.value }.keys.toSet())
        editor.apply()
    }

    // Favorileri SharedPreferences'ten yükle
    private fun loadFavorites(): MutableMap<String, Boolean> {
        val savedFavorites = sharedPreferences.getStringSet("favorites", setOf()) ?: setOf()
        return songs.associateWith { savedFavorites.contains(it) }.toMutableMap()
    }
}
