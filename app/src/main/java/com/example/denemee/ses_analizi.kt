package com.example.denemee

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import kotlin.concurrent.thread

class ses_analizi : AppCompatActivity() {
    private lateinit var frequencyTextView: TextView
    private lateinit var volumeProgressBar: ProgressBar
    private lateinit var dispatcher: AudioDispatcher

    // Piyano seslerinin frekans aralığı (A0'dan C8'e kadar)
    private val minFreq = 27.5f   // A0
    private val maxFreq = 4186f   // C8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ses_analizi)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        frequencyTextView = findViewById(R.id.frequencyTextView)
        volumeProgressBar = findViewById(R.id.volumeProgressBar)

        // Mikrofon izninin kontrolü
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        } else {
            startFrequencyDetection()
        }

        val button = findViewById<ImageButton>(R.id.back1Button5)
        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startFrequencyDetection() {
        val sampleRate = 44100F
        val bufferSize = 7056   // Buffer boyutunu 1024 olarak ayarlıyoruz
        val overlap = 0

        try {
            // Mikrofon girişini başlatma
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                sampleRate.toInt(), bufferSize, overlap
            )

            // Frekans algılama işlemi
            val pitchHandler = PitchDetectionHandler { result, _ ->
                val pitchInHz = result.pitch
                runOnUiThread {
                    if (pitchInHz > 0) {
                        frequencyTextView.text = "Frequency: %.2f Hz".format(pitchInHz)

                        // ProgressBar değerini frekansa göre ayarla
                        val normalizedValue = normalizeFrequencyToProgressBar(pitchInHz)
                        volumeProgressBar.progress = normalizedValue
                    } else {
                        frequencyTextView.text = "No pitch detected"
                        volumeProgressBar.progress = 0 // Eğer frekans tespit edilmezse ProgressBar sıfırlanır
                    }
                }
            }

            // PitchProcessor kullanarak frekans tespiti
            val pitchProcessor = PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.YIN,
                sampleRate,
                bufferSize,
                pitchHandler
            )
            dispatcher.addAudioProcessor(pitchProcessor)

            // Dispatcher'ı bir iş parçacığında çalıştır
            thread(start = true) {
                dispatcher.run()
            }
        } catch (e: Exception) {
            runOnUiThread {
                frequencyTextView.text = "Error: ${e.message}"
            }
        }
    }

    // Frekansı 27.5 Hz (A0) ile 4186 Hz (C8) arasına normalize ederek ProgressBar için 0-100 aralığına dönüştürme
    private fun normalizeFrequencyToProgressBar(frequency: Float): Int {
        // Frekans, minFreq ve maxFreq arasında olup olmadığını kontrol et
        val normalizedValue = ((frequency - minFreq) / (maxFreq - minFreq) * 100).toInt()

        // Eğer frekans sınırlar dışında ise ProgressBar değerini 0 veya 100 yap
        return normalizedValue.coerceIn(0, 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        dispatcher.stop() // Dispatcher'ı durdur
    }
}
