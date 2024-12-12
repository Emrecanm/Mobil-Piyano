package com.example.denemee

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import java.io.File
import kotlin.concurrent.thread

@Suppress("DEPRECATION")
class ses_analizi : AppCompatActivity() {
    private lateinit var frequencyTextView: TextView
    private lateinit var volumeProgressBar: ProgressBar
    private lateinit var dispatcher: AudioDispatcher
    private lateinit var startAnalysisButton: Button
    private lateinit var stopAnalysisButton: Button
    private lateinit var startFileAnalysisButton: Button
    private lateinit var frequencyListTextView: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var frequencyListTextViewRight: TextView
    private lateinit var scrollViewRight: ScrollView

    private val minFreq = 25f   // A0
    private val maxFreq = 4200f   // C8
    private var selectedAudioUri: Uri? = null // Seçilen dosya URI'si

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
        frequencyListTextView = findViewById(R.id.frequencyListTextView)
        scrollView = findViewById(R.id.frequencyScrollView)
        startAnalysisButton = findViewById(R.id.startAnalysisButton)
        stopAnalysisButton = findViewById(R.id.stopAnalysisButton)
        startFileAnalysisButton = findViewById(R.id.startFileAnalysisButton) // Yeni buton

        // Mikrofon izni kontrolü
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }

        startAnalysisButton.setOnClickListener {
            startFrequencyDetection()
        }

        stopAnalysisButton.setOnClickListener {
            stopFrequencyDetection()
        }

        startFileAnalysisButton.setOnClickListener {
            if (selectedAudioUri != null) {
                analyzeAudioFile(selectedAudioUri!!)
            } else {
                frequencyTextView.text = "Önce Dosya Seçin!"
            }
        }

        val backButton = findViewById<ImageButton>(R.id.back1Button5)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val fileSelectButton = findViewById<ImageButton>(R.id.selectFileButton)
        fileSelectButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            startActivityForResult(Intent.createChooser(intent, "Ses Dosyası Seç"), 2)
        }
    }

    private fun startFrequencyDetection() {
        val sampleRate = 44100F
        val bufferSize = 7056
        val overlap = 0
        var currentTime = 0f // Zamanı takip etmek için

        try {
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                sampleRate.toInt(), bufferSize, overlap
            )

            val pitchHandler = PitchDetectionHandler { result, _ ->
                val pitchInHz = result.pitch
                runOnUiThread {
                    if (pitchInHz > 0) {
                        frequencyTextView.text = "Frekans: %.2f Hz".format(pitchInHz)
                        val normalizedValue = normalizeFrequencyToProgressBar(pitchInHz)
                        volumeProgressBar.progress = normalizedValue
                        frequencyListTextView.append("Zaman: %.2f s\n Frekans: %.2f Hz\n\n".format(currentTime,pitchInHz))
                        scrollView.post {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                        }
                    } else {
                        frequencyTextView.text = "Frekans Algılanamadı"
                        volumeProgressBar.progress = 0
                    }
                }
                currentTime += bufferSize.toFloat() / sampleRate // Her buffer için geçen süreyi hesapla
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


    private fun stopFrequencyDetection() {
        if (::dispatcher.isInitialized && !dispatcher.isStopped) {
            dispatcher.stop()
            runOnUiThread {
                frequencyTextView.text = "Analiz Durduruldu."
                volumeProgressBar.progress = 0
            }
        }
    }

    private fun normalizeFrequencyToProgressBar(frequency: Float): Int {
        val normalizedValue = ((frequency - minFreq) / (maxFreq - minFreq) * 100).toInt()
        return normalizedValue.coerceIn(0, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK) {
            selectedAudioUri = data?.data
            if (selectedAudioUri != null) {
                frequencyTextView.text = "Dosya başarıyla seçildi."
            } else {
                frequencyTextView.text = "Dosya seçilemedi."
            }
        }
    }

    @SuppressLint("SetTextI18n", "Recycle")
    private fun analyzeAudioFile(audioUri: Uri) {
        try {
            val fileDescriptor = contentResolver.openFileDescriptor(audioUri, "r")?.fileDescriptor

            if (fileDescriptor != null) {
                val sampleRate = 44100F
                val bufferSize = 1024
                val results = mutableListOf<Pair<Float, Float>>()

                val audioFile = File(audioUri.path)
                if (!audioFile.exists()) {
                    runOnUiThread {
                        frequencyTextView.text = "Error: Dosya Bulunamadı!"
                    }
                    return
                }

                val filePath = audioFile.absolutePath
                dispatcher = AudioDispatcherFactory.fromPipe(filePath, sampleRate.toInt(), bufferSize, 0)

                var currentTime = 0f
                val timePerBuffer = bufferSize.toFloat() / sampleRate

                val pitchHandler = PitchDetectionHandler { result, _ ->
                    val pitchInHz = result.pitch
                    if (pitchInHz > 0) {
                        results.add(Pair(currentTime, pitchInHz))
                        runOnUiThread {
                            frequencyListTextView.append("Zaman: %.2f s\nFrekans: %.2f Hz\n\n".format(currentTime, pitchInHz))
                            frequencyListTextViewRight.append("Zaman: %.2f s\n Frekans: %.2f Hz\n\n".format(currentTime, pitchInHz))
                            scrollViewRight.post {
                                scrollViewRight.fullScroll(ScrollView.FOCUS_DOWN)
                            }
                        }
                    }
                    currentTime += timePerBuffer
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
            } else {
                runOnUiThread {
                    frequencyTextView.text = "Error: Dosya Uzantısı Açılamıyor"
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                frequencyTextView.text = "Error: ${e.message}"
            }
        }
    }
}