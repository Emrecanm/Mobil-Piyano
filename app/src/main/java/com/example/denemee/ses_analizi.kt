package com.example.denemee

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
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

@Suppress("DEPRECATION")
class ses_analizi : AppCompatActivity() {
    private lateinit var frequencyTextView: TextView // Frekans verisini gösterecek TextView
    private lateinit var volumeProgressBar: ProgressBar // Ses seviyesi göstergesi
    private lateinit var dispatcher: AudioDispatcher // Ses verilerini işlemek için dispatcher
    private lateinit var startAnalysisButton: Button // Frekans analizini başlatan buton
    private lateinit var stopAnalysisButton: Button // Frekans analizini durduran buton
    private lateinit var startFileAnalysisButton: Button // Dosya analizi başlatan buton
    private lateinit var frequencyListTextView: TextView // Frekans listesini gösterecek TextView
    private lateinit var scrollView: ScrollView // Frekans listesini kaydırmak için ScrollView

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Uygulama kenarından kenara görünüm
        setContentView(R.layout.activity_ses_analizi)

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
        scrollView = findViewById(R.id.frequencyScrollView)
        startAnalysisButton = findViewById(R.id.startAnalysisButton)
        stopAnalysisButton = findViewById(R.id.stopAnalysisButton)
        startFileAnalysisButton = findViewById(R.id.startFileAnalysisButton)
// dosya analizi
        startFileAnalysisButton.setOnClickListener {
            selectAudioFile()
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

    // Frekans tespitini başlatan fonksiyon
    private fun startFrequencyDetection() {
        val sampleRate = 44100F // Örnekleme hızı
        val bufferSize = 7056 // Buffer boyutu
        val overlap = 0 // Örtüşme oranı
        var currentTime = 0f // Zamanı takip etmek için

        try {
            // Mikrofon verisi almak için dispatcher oluşturuluyor
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                sampleRate.toInt(), bufferSize, overlap
            )

            val pitchHandler = PitchDetectionHandler { result, _ ->
                val pitchInHz = result.pitch // Frekans verisini al
                runOnUiThread {
                    if (pitchInHz > 0) {
                        // Frekansı notaya dönüştür
                        val noteName = getNoteNameFromFrequency(pitchInHz)
                        frequencyTextView.text = "Nota: $noteName (%.2f Hz)".format(pitchInHz)
                        val normalizedValue = normalizeFrequencyToProgressBar(pitchInHz)
                        volumeProgressBar.progress = normalizedValue
                        frequencyListTextView.append("Zaman: %.2f s\nNota: $noteName\n\n".format(currentTime))
                        scrollView.post {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN) // ScrollView kaydırma
                        }
                    } else {
                        frequencyTextView.text = "Nota Algılanamadı"
                        volumeProgressBar.progress = 0
                    }
                }
                currentTime += bufferSize.toFloat() / sampleRate // Zamanı güncelle
            }

            val pitchProcessor = PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.YIN,
                sampleRate,
                bufferSize,
                pitchHandler
            )
            dispatcher.addAudioProcessor(pitchProcessor)

            // Ses verilerini işlemek için dispatcher'ı başlat
            thread(start = true) {
                dispatcher.run()
            }
        } catch (e: Exception) {
            runOnUiThread {
                frequencyTextView.text = "Error: ${e.message}" // Hata mesajı
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

    private fun selectAudioFile() {
        println("Dosya seçimi yap")
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
        }
        startActivityForResult(intent, 123) // 123 koduyla dosya seçimini başlatıyoruz
    }
}
