import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.denemee.R

class SongAdapter(
    private val songs: MutableList<String>,  // Şarkı listemiz MutableList oldu çünkü öğeleri değiştireceğiz
    private val onFavoriteClick: (String, Any?) -> Unit,
    private val onPlayClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onListenClick: (String) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    // Şarkıların favori durumlarını saklamak için bir harita (map)
    private val favoriteStates = mutableMapOf<String, Boolean>()

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

        // Favori durumu kontrol et ve ikonunu güncelle
        val isFavorite = favoriteStates[song] ?: false
        holder.buttonFavorite.setImageResource(
            if (isFavorite) R.drawable.bookmark2 else R.drawable.bookmark
        )

        holder.songName.text = song

        // Favori butonunun tıklanma olayı
        holder.buttonFavorite.setOnClickListener {
            val newState = !(favoriteStates[song] ?: false)
            favoriteStates[song] = newState

            // Favori durumu değiştikçe, RecyclerView'deki öğeyi güncelle
            notifyItemChanged(position)

            // Favori ikonunu güncelle
            holder.buttonFavorite.setImageResource(
                if (newState) R.drawable.bookmark2 else R.drawable.bookmark
            )

            // Favori işlemi yapıldığında işlemi gerçekleştirebilirsiniz
            if (newState) {
                onFavoriteClick(song, "Favorilere eklendi!")
            } else {
                onFavoriteClick(song, "Favorilerden kaldırıldı!")
            }
        }

        holder.buttonListen.setOnClickListener { onListenClick(song) }

        // Silme butonunun tıklanma olayı
        holder.buttonDelete.setOnClickListener {
            // Silme işlemi için AlertDialog açılacak
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Silme İşlemi")
                .setMessage("$song şarkısını silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet") { dialog, which ->
                    // Evet tıklanırsa, şarkıyı listeden sil
                    songs.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, songs.size)  // Listeyi güncelle
                    onDeleteClick(song)
                }
                .setNegativeButton("Hayır", null)
                .show()
        }

        holder.buttonPlay.setOnClickListener { onPlayClick(song) }
    }

    override fun getItemCount(): Int = songs.size
}
