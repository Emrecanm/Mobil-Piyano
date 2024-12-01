package com.example.denemee

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
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
import java.io.FileDescriptor
import java.io.InputStream
import kotlin.concurrent.thread

class ses_analizi : AppCompatActivity() {
    private lateinit var frequencyTextView: TextView
    private lateinit var volumeProgressBar: ProgressBar
    private lateinit var dispatcher: AudioDispatcher

    // Piyano seslerinin frekans aralığı (A0'dan C8'e kadar)
    private val minFreq = 25f   // A0
    private val maxFreq = 4200f   // C8

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

        // Mikrofon analizi için geri dön
        val backButton = findViewById<ImageButton>(R.id.back1Button5)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Ses dosyası seçme butonu
        val fileSelectButton = findViewById<ImageButton>(R.id.selectFileButton)
        fileSelectButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            startActivityForResult(Intent.createChooser(intent, "Ses Dosyası Seç"), 2)
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

    // Frekansı normalize ederek ProgressBar için 0-100 aralığına dönüştürme
    private fun normalizeFrequencyToProgressBar(frequency: Float): Int {
        val normalizedValue = ((frequency - minFreq) / (maxFreq - minFreq) * 100).toInt()
        return normalizedValue.coerceIn(0, 100)
    }

    // Seçilen dosyayı analiz et
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK) {
            val audioUri = data?.data
            if (audioUri != null) {
                analyzeAudioFile(audioUri)
            } else {
                frequencyTextView.text = "Dosya seçilemedi."
            }
        }
    }

    private fun analyzeAudioFile(audioUri: Uri) {
        try {
            // Uri'yi FileDescriptor'a dönüştür
            val fileDescriptor = contentResolver.openFileDescriptor(audioUri, "r")?.fileDescriptor

            // Eğer fileDescriptor null ise hata mesajı ver
            if (fileDescriptor != null) {
                val sampleRate = 44100F  // Örnekleme hızı
                val bufferSize = 1024    // Buffer boyutu

                // Ses dosyasını fromPipe ile analiz et
                dispatcher = AudioDispatcherFactory.fromPipe(fileDescriptor.toString(), sampleRate.toInt(), bufferSize, 0)

                // PitchDetectionHandler ile frekans algılama işlemi
                val pitchHandler = PitchDetectionHandler { result, _ ->
                    val pitchInHz = result.pitch
                    runOnUiThread {
                        if (pitchInHz > 0) {
                            frequencyTextView.text = "Frequency: %.2f Hz".format(pitchInHz)
                            val normalizedValue = normalizeFrequencyToProgressBar(pitchInHz)
                            volumeProgressBar.progress = normalizedValue
                        } else {
                            frequencyTextView.text = "No pitch detected"
                            volumeProgressBar.progress = 0
                        }
                    }
                }

                // PitchProcessor ile frekans analizi
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
            } else {
                // FileDescriptor alınamadıysa hata mesajı göster
                runOnUiThread {
                    frequencyTextView.text = "Error: Unable to open file descriptor"
                }
            }
        } catch (e: Exception) {
            // Hata durumunda UI'yi güncelle
            runOnUiThread {
                frequencyTextView.text = "Error: ${e.message}"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dispatcher.stop() // Dispatcher'ı durdur
    }
}
