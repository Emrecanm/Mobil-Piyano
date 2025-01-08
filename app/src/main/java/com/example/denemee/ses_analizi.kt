package com.example.denemee

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import kotlin.concurrent.thread
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import java.io.File

@Suppress("DEPRECATION")
class ses_analizi : AppCompatActivity() {
    private lateinit var frequencyTextView: TextView // Frekans verisini gösterecek TextView
    private lateinit var volumeProgressBar: ProgressBar // Ses seviyesi göstergesi
    private lateinit var dispatcher: AudioDispatcher // Ses verilerini işlemek için dispatcher
    private lateinit var startAnalysisButton: Button // Frekans analizini başlatan buton
    private lateinit var stopAnalysisButton: Button // Frekans analizini durduran buton
    private lateinit var frequencyListTextView: TextView // Sol Frekans listesini gösterecek TextView
    private lateinit var frequencyListTextViewRight:TextView // Sağ Frekans listesini gösterecek TextView
    private lateinit var scrollView: ScrollView // Frekans listesini kaydırmak için ScrollView
    private lateinit var scrollViewRight: ScrollView // Sağ scrollwiev
    private lateinit var selectFileButton: ImageButton //Dosya seçimi yapmak için Buton
    private lateinit var mediaPlayer: MediaPlayer // Global olarak tanımlama
    private lateinit var startFileCompareButton: Button //Sol scrollView ile sağ scrollviwi karşılaştırmak için Buton
    private lateinit var insertNota: ImageButton //Otomatik scrollView i doldurma
    private lateinit var icerigiKaydet: ImageButton //analiz edilen verileri kaydetmek için kullanılacak
    val REQUEST_CODE_PICK_SONG = 1

    private val minFreq = 25f   // A0 notasının frekansı
    private val maxFreq = 4200f   // C8 notasının frekansı

    // Nota-frekans eşleştirmesi
    private val noteFrequencies = listOf(
        Pair("A0", 27.50f), Pair("A#0", 29.14f), Pair("B0", 30.87f),
        Pair("C1", 32.70f), Pair("C#1", 34.65f), Pair("D1", 36.71f), Pair("D#1", 38.89f),
        Pair("E1", 41.20f), Pair("F1", 43.65f), Pair("F#1", 46.25f), Pair("G1", 49.00f),
        Pair("G#1", 51.91f), Pair("A1", 55.00f), Pair("A#1", 58.27f), Pair("B1", 61.74f),
        Pair("C2", 65.41f), Pair("C#2", 69.30f), Pair("D2", 73.42f), Pair("D#2", 77.78f),
        Pair("E2", 82.41f), Pair("F2", 87.31f), Pair("F#2", 92.50f), Pair("G2", 98.00f),
        Pair("G#2", 103.83f), Pair("A2", 110.00f), Pair("A#2", 116.54f), Pair("B2", 123.47f),
        Pair("C3", 130.81f), Pair("C#3", 138.59f), Pair("D3", 146.83f), Pair("D#3", 155.56f),
        Pair("E3", 164.81f), Pair("F3", 174.61f), Pair("F#3", 185.00f), Pair("G3", 196.00f),
        Pair("G#3", 207.65f), Pair("A3", 220.00f), Pair("A#3", 233.08f), Pair("B3", 246.94f),
        Pair("C4", 261.63f), Pair("C#4", 277.18f), Pair("D4", 293.66f), Pair("D#4", 311.13f),
        Pair("E4", 329.63f), Pair("F4", 349.23f), Pair("F#4", 369.99f), Pair("G4", 392.00f),
        Pair("G#4", 415.30f), Pair("A4", 440.00f), Pair("A#4", 466.16f), Pair("B4", 493.88f),
        Pair("C5", 523.25f), Pair("C#5", 554.37f), Pair("D5", 587.33f), Pair("D#5", 622.25f),
        Pair("E5", 659.26f), Pair("F5", 698.46f), Pair("F#5", 739.99f), Pair("G5", 783.99f),
        Pair("G#5", 830.61f), Pair("A5", 880.00f), Pair("A#5", 932.33f), Pair("B5", 987.77f),
        Pair("C6", 1046.50f), Pair("C#6", 1108.73f), Pair("D6", 1174.66f), Pair("D#6", 1244.51f),
        Pair("E6", 1318.51f), Pair("F6", 1396.91f), Pair("F#6", 1479.98f), Pair("G6", 1567.98f),
        Pair("G#6", 1661.22f), Pair("A6", 1760.00f), Pair("A#6", 1864.66f), Pair("B6", 1975.53f),
        Pair("C7", 2093.00f), Pair("C#7", 2217.46f), Pair("D7", 2349.32f), Pair("D#7", 2489.02f),
        Pair("E7", 2637.02f), Pair("F7", 2793.83f), Pair("F#7", 2959.96f), Pair("G7", 3135.96f),
        Pair("G#7", 3322.44f), Pair("A7", 3520.00f), Pair("A#7", 3729.31f), Pair("B7", 3951.07f),
        Pair("C8", 4186.01f)
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_SONG && resultCode == Activity.RESULT_OK) {
            data?.data?.let { songUri ->
                val songName = getSongName(songUri)
                val intent = Intent(this, SarkiListesi::class.java)
                intent.putExtra("SONG_NAME", songName)
                intent.putExtra("SONG_URI", songUri.toString())
                startActivity(intent)
            }
        }
    }

    private fun getSongName(songUri: Uri): String {
        val cursor = contentResolver.query(songUri, null, null, null, null)
        cursor?.moveToFirst()
        val songTitleIndex = cursor?.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val songTitle = if (songTitleIndex != null && songTitleIndex != -1) {
            cursor.getString(songTitleIndex)
        } else {
            "Şarkı Adı Bulunamadı"
        }
        cursor?.close()
        return songTitle
    }


    @SuppressLint("CutPasteId", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Uygulama kenarından kenara görünüm
        setContentView(R.layout.activity_ses_analizi)
      val  btnSelectSong = findViewById<ImageButton>(R.id.btn_select_song)
        btnSelectSong.setOnClickListener {
            // Şarkı seçmek için dosya seçici açma
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_PICK_SONG)
        }
        // Geri butonunu bulma ve tıklama olayı
        val backButton = findViewById<ImageButton>(R.id.back1Button2)
        backButton?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Sistem çubuğunun görünümünü düzenleyen kod
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // UI bileşenlerinin tanımlanması
        frequencyTextView = findViewById(R.id.frequencyTextView)
        volumeProgressBar = findViewById(R.id.volumeProgressBar)
        frequencyListTextView = findViewById(R.id.frequencyListTextView)
        frequencyListTextViewRight=findViewById(R.id.frequencyListTextViewRight)
        startFileCompareButton = findViewById(R.id.startFileCompareButton)
        scrollView = findViewById(R.id.frequencyScrollView)
        scrollViewRight=findViewById(R.id.frequencyScrollViewRight)
        startAnalysisButton = findViewById(R.id.startAnalysisButton)
        stopAnalysisButton = findViewById(R.id.stopAnalysisButton)
        insertNota=findViewById(R.id.insertNota)
        icerigiKaydet=findViewById(R.id.icerigiKaydet)

        val selectedSong = intent.getStringExtra("songName") // Favorilerden gelen şarkıyı aldık.

// Şarkıya göre medya kaynağını belirleyin
        val mediaResId = when (selectedSong) {
            "KARGA" -> R.raw.karga
            "KARGA2" -> R.raw.kargahatali
            "KARGA3" -> R.raw.kargahatali2
            else -> null
        }
        val fileName = "$selectedSong.txt"
        val file = File(filesDir, fileName)

        if (!file.exists()) {
            if (mediaResId != null) {
                // MediaPlayer ile seçilen şarkıyı çal
                mediaPlayer = MediaPlayer.create(this, mediaResId)
                mediaPlayer.start()

                // Frekans analizini başlat
                startFrequencyDetectionSong()

                mediaPlayer.setOnCompletionListener {
                    // Frekans analizini durdur
                    stopFrequencyDetection()
                }
            } else {
                Toast.makeText(this, "Geçersiz şarkı seçimi.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Eğer kayıt dosyası varsa kullanıcıya bilgi ver
            Toast.makeText(this, "Bu şarkı için kayıt mevcut. Medya oynatılmıyor.", Toast.LENGTH_SHORT).show()
        }

        icerigiKaydet.setOnClickListener{
            //BURAYA SCROLLVİEWLERİN İÇERİKLERİNİ KADEDECEK
           val frequencyScrollView=findViewById<ScrollView>(R.id.frequencyScrollView)
            val frequencyListTextViewRight = findViewById<TextView>(R.id.frequencyListTextViewRight)

            val content = frequencyListTextViewRight.text.toString()
            val fileName = "$selectedSong.txt"
            val file = File(filesDir, fileName)

            try {
                file.writeText(content)
                Toast.makeText(this, "İçerik başarıyla kaydedildi.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "İçerik kaydedilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        //Önceden hazırlanmış notaları çağırığ direk yazdırma.
        insertNota.setOnClickListener{
            val frequencyListTextViewRight = findViewById<TextView>(R.id.frequencyListTextViewRight)

            val fileName = "$selectedSong.txt"
            val file = File(filesDir, fileName)

            if (file.exists()) {
                // Dosya içeriğini oku ve frequencyScrollView'a yazdır
                val content = file.readText()
                frequencyListTextViewRight.text = content
            } else {
                // Hata mesajı göster
                Toast.makeText(this, "Dosya bulunamadı. Lütfen kayıt edin.", Toast.LENGTH_SHORT).show()
            }        }

        startFileCompareButton.setOnClickListener {
            // ScrollView ve ScrollViewRight içeriklerini al
            val scrollViewText = findViewById<TextView>(R.id.frequencyListTextView)
            val scrollViewRightText = findViewById<TextView>(R.id.frequencyListTextViewRight)

            val leftContent = scrollViewText.text.toString()
            val rightContent = scrollViewRightText.text.toString()

            // Verileri yeni aktiviteye gönder
            val intent = Intent(this, activity_comparison_result::class.java).apply {
                putExtra("leftContent", leftContent)
                putExtra("rightContent", rightContent)
            }
            startActivity(intent)
        }



        // Mikrofon izni kontrolü
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1 // Mikrofon izni isteği
            )
        }

        // Frekans analizini başlatma butonuna tıklama işlemi
        startAnalysisButton.setOnClickListener {
            startFrequencyDetection()
        }

        // Frekans analizini durdurma butonuna tıklama işlemi
        stopAnalysisButton.setOnClickListener {
            stopFrequencyDetection()
        }
    }

    // Frekansı notaya dönüştüren fonksiyon
    private fun getNoteNameFromFrequency(frequency: Float): String {
        var closestNote = "Bilinmiyor"
        var smallestDifference = Float.MAX_VALUE
        for ((note, freq) in noteFrequencies) {
            val difference = kotlin.math.abs(freq - frequency)
            if (difference < smallestDifference) {
                smallestDifference = difference
                closestNote = note
            }
        }
        return closestNote
    }

    // Son algılanan notayı saklamak için bir değişken
    private var lastDetectedNote: String? = null

    private fun startFrequencyDetectionSong() {
        val sampleRate = 44100F // Örnekleme hızı
        val bufferSize = 7056 // Buffer boyutu
        val overlap = 0 // Örtüşme oranı
        var currentTime = 0f // Zamanı takip etmek için

        try {
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                sampleRate.toInt(), bufferSize, overlap
            )

            val pitchHandler = PitchDetectionHandler { result, _ ->
                val pitchInHz = result.pitch
                runOnUiThread {
                    if (pitchInHz > 0) {
                        val noteName = getNoteNameFromFrequency(pitchInHz)

                        // Eğer yeni nota, son algılanan notadan farklıysa
                        if (noteName != lastDetectedNote) {
                            lastDetectedNote = noteName // Son algılanan notayı güncelle
                            frequencyTextView.text = "Nota: $noteName (%.2f Hz)".format(pitchInHz)
                            val normalizedValue = normalizeFrequencyToProgressBar(pitchInHz)
                            volumeProgressBar.progress = normalizedValue
                            frequencyListTextViewRight.append(
                                "⏱️: %.2f saniye\n \uD834\uDD1E : $noteName (%.2f Hz) notası\n••••••••••••••••••••••••••••\n".format(currentTime,pitchInHz)
                            )
                            scrollViewRight.post {
                                scrollViewRight.fullScroll(ScrollView.FOCUS_DOWN)
                            }
                        }
                    } else {
                        // Eğer nota algılanmazsa son notayı sıfırla
                        lastDetectedNote = null
                        frequencyTextView.text = "Nota Algılanamadı"
                        volumeProgressBar.progress = 0
                    }
                }
                currentTime += bufferSize.toFloat() / sampleRate
            }

            val pitchProcessor = PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.YIN,
                sampleRate,
                bufferSize,
                pitchHandler
            )
            dispatcher.addAudioProcessor(pitchProcessor)

            thread(start = true) {
                dispatcher.run()
            }
        } catch (e: Exception) {
            runOnUiThread {
                frequencyTextView.text = "Error: ${e.message}"
            }
        }
    }



    private fun startFrequencyDetection() {
        val sampleRate = 44100F // Örnekleme hızı
        val bufferSize = 7056 // Buffer boyutu
        val overlap = 0 // Örtüşme oranı
        var currentTime = 0f // Zamanı takip etmek için
        var lastDetectedNote: String? = null // En son algılanan nota
        var analysisDuration = 0f // Analizin ne kadar süreceğini belirleyen değişken
        var isAnalysisCompleted = false // Analizin tamamlanıp tamamlanmadığını takip etmek için

        // FrequencyListTextViewRight içeriğini kontrol et
        val scrollViewContent = frequencyListTextViewRight.text.toString()
        if (scrollViewContent.isNotEmpty()) {
            // Son girdinin süresini bul
            val lastEntry = scrollViewContent.split("\n").lastOrNull()

            lastEntry?.let {
                val match = Regex("⏱️: (\\d+\\.\\d+) saniye").find(it)
                if (match != null) {
                    try {
                        analysisDuration = match.groupValues[1].toFloat()
                    } catch (e: NumberFormatException) {
                    }
                }
            }
        }

        // Eğer süre 0 ise, en son girilen süreyi kullan
        if (analysisDuration <= 0) {
            val allEntries = scrollViewContent.split("\n").filter { it.contains("⏱️:") }
            if (allEntries.isNotEmpty()) {
                val lastDurationEntry = allEntries.last()
                val match = Regex("⏱️: (\\d+\\.\\d+) saniye").find(lastDurationEntry)
                if (match != null) {
                    try {
                        analysisDuration = match.groupValues[1].toFloat()
                    } catch (e: NumberFormatException) {
                    }
                }
            }
        }

        // Eğer hala geçerli bir süre yoksa, varsayılan süreyi kullan
        if (analysisDuration <= 0) {
            analysisDuration = 99999f // Varsayılan 99999 saniye
        }

        try {
            // Dispatcher'ı başlat
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                sampleRate.toInt(), bufferSize, overlap
            )

            val pitchHandler = PitchDetectionHandler { result, _ ->
                val pitchInHz = result.pitch
                runOnUiThread {
                    // Eğer süre dolmuşsa analizi durdur
                    if (!isAnalysisCompleted && currentTime >= analysisDuration) {
                        isAnalysisCompleted = true
                        dispatcher.stop() // Dispatcher'ı durdur
                        frequencyTextView.text = "Analiz tamamlandı."
                        return@runOnUiThread
                    }

                    if (pitchInHz > 0) {
                        val noteName = getNoteNameFromFrequency(pitchInHz)

                        // Eğer yeni nota, son algılanan notadan farklıysa
                        if (noteName != lastDetectedNote) {
                            lastDetectedNote = noteName // Son algılanan notayı güncelle
                            frequencyTextView.text = "Nota: $noteName (%.2f Hz)".format(pitchInHz)
                            val normalizedValue = normalizeFrequencyToProgressBar(pitchInHz)
                            volumeProgressBar.progress = normalizedValue
                            frequencyListTextView.append(
                                "⏱️: %.2f saniye\n \uD834\uDD1E : $noteName (%.2f Hz) notası\n••••••••••••••••••••••••••••\n".format(currentTime,pitchInHz)
                            )
                            scrollView.post {
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                            }
                        }
                    } else {
                        // Eğer nota algılanmazsa son notayı sıfırla
                        frequencyTextView.text = "Nota Algılanamadı"
                        volumeProgressBar.progress = 0
                        lastDetectedNote = null
                    }
                }
                // Zamanı güncelle
                currentTime += bufferSize.toFloat() / sampleRate
            }

            val pitchProcessor = PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.YIN,
                sampleRate,
                bufferSize,
                pitchHandler
            )
            dispatcher.addAudioProcessor(pitchProcessor)

            // Dispatcher'ı çalıştır
            thread(start = true) {
                dispatcher.run()
            }
        } catch (e: Exception) {
            runOnUiThread {
                frequencyTextView.text = "Error: ${e.message}"
            }
        }
    }


    // Frekans tespitini durduran fonksiyon
    private fun stopFrequencyDetection() {
        if (::dispatcher.isInitialized && !dispatcher.isStopped) {
            dispatcher.stop() // Dispatcher'ı durdur
            runOnUiThread {
                frequencyTextView.text = "Analiz Durduruldu."
                volumeProgressBar.progress = 0
            }
        }
    }

    // Frekans değerini ProgressBar'a normalize eden fonksiyon
    private fun normalizeFrequencyToProgressBar(frequency: Float): Int {
        val normalizedValue = ((frequency - minFreq) / (maxFreq - minFreq) * 100).toInt()
        return normalizedValue.coerceIn(0, 100) // Değeri 0-100 arasında kısıtla
    }
}
