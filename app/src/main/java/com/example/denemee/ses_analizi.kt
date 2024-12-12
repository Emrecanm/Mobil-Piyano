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
    private lateinit var frequencyTextView: TextView // Frekans verisini gösterecek TextView
    private lateinit var volumeProgressBar: ProgressBar // Ses seviyesi göstergesi
    private lateinit var dispatcher: AudioDispatcher // Ses verilerini işlemek için dispatcher
    private lateinit var startAnalysisButton: Button // Frekans analizini başlatan buton
    private lateinit var stopAnalysisButton: Button // Frekans analizini durduran buton
    private lateinit var startFileAnalysisButton: Button // Dosya analizi başlatan buton
    private lateinit var frequencyListTextView: TextView // Frekans listesini gösterecek TextView
    private lateinit var scrollView: ScrollView // Frekans listesini kaydırmak için ScrollView
    private lateinit var frequencyListTextViewRight: TextView // Sağdaki frekans listesini gösterecek TextView
    private lateinit var scrollViewRight: ScrollView // Sağdaki frekans listesini kaydırmak için ScrollView

    private val minFreq = 25f   // A0 notasının frekansı
    private val maxFreq = 4200f   // C8 notasının frekansı
    private var selectedAudioUri: Uri? = null // Seçilen ses dosyasının URI'si

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

        // Dosya analizi başlatma butonuna tıklama işlemi
        startFileAnalysisButton.setOnClickListener {
            if (selectedAudioUri != null) {
                analyzeAudioFile(selectedAudioUri!!)
            } else {
                frequencyTextView.text = "Önce Dosya Seçin!" // Dosya seçilmediyse mesaj
            }
        }

        // Geri dönme butonuna tıklama işlemi
        val backButton = findViewById<ImageButton>(R.id.back1Button5)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Dosya seçme butonuna tıklama işlemi
        val fileSelectButton = findViewById<ImageButton>(R.id.selectFileButton)
        fileSelectButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*" // Ses dosyası seçme
            startActivityForResult(Intent.createChooser(intent, "Ses Dosyası Seç"), 2)
        }
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
                        // UI'yi güncelle
                        frequencyTextView.text = "Frekans: %.2f Hz".format(pitchInHz)
                        val normalizedValue = normalizeFrequencyToProgressBar(pitchInHz)
                        volumeProgressBar.progress = normalizedValue
                        frequencyListTextView.append("Zaman: %.2f s\n Frekans: %.2f Hz\n\n".format(currentTime, pitchInHz))
                        scrollView.post {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN) // ScrollView kaydırma
                        }
                    } else {
                        frequencyTextView.text = "Frekans Algılanamadı"
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

    // Dosya seçimi sonucu gelen veriyi işleyen fonksiyon
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK) {
            selectedAudioUri = data?.data // Seçilen dosyanın URI'sini al
            if (selectedAudioUri != null) {
                frequencyTextView.text = "Dosya başarıyla seçildi."
            } else {
                frequencyTextView.text = "Dosya seçilemedi."
            }
        }
    }

    // Ses dosyasını analiz eden fonksiyon
    @SuppressLint("SetTextI18n", "Recycle")
    private fun analyzeAudioFile(audioUri: Uri) {
        try {
            // Ses dosyasını açmak için file descriptor elde edilmesi
            val fileDescriptor = contentResolver.openFileDescriptor(audioUri, "r")?.fileDescriptor

            // Dosya descriptor'ü null değilse, dosya mevcut demektir
            if (fileDescriptor != null) {
                val sampleRate = 44100F // Ses örnekleme hızı (44100 Hz, standart)
                val bufferSize = 7056 // Veri bloğu büyüklüğü
                val results = mutableListOf<Pair<Float, Float>>() // Frekans analiz sonuçlarını tutacak liste

                // Ses dosyasını dosya yolu ile açıyoruz
                val audioFile = File(audioUri.path)

                // Dosya mevcut değilse hata mesajı gösteriliyor
                if (!audioFile.exists()) {
                    runOnUiThread {
                        frequencyTextView.text = "Error: Dosya Bulunamadı!"
                    }
                    return
                }

                // Dosya yolunu alıyoruz
                val filePath = audioFile.absolutePath
                // Ses dosyasını analiz etmek için dispatcher oluşturuluyor
                dispatcher = AudioDispatcherFactory.fromPipe(filePath, sampleRate.toInt(), bufferSize, 0)

                var currentTime = 0f // Geçerli zaman (saniye)
                val timePerBuffer = bufferSize.toFloat() / sampleRate // Her veri bloğu için geçen süre (saniye)

                // Frekans tespiti için pitchHandler tanımlanıyor
                val pitchHandler = PitchDetectionHandler { result, _ ->
                    val pitchInHz = result.pitch // Tespit edilen frekans (Hz cinsinden)
                    if (pitchInHz > 0) {
                        // Geçerli frekans pozitifse, sonucu kaydediyoruz
                        results.add(Pair(currentTime, pitchInHz))
                        runOnUiThread {
                            // UI'yi güncelliyoruz: sol ve sağ frekans listesine ekliyoruz
                            frequencyListTextView.append("Zaman: %.2f s\nFrekans: %.2f Hz\n\n".format(currentTime, pitchInHz))
                            frequencyListTextViewRight.append("Zaman: %.2f s\n Frekans: %.2f Hz\n\n".format(currentTime, pitchInHz))
                            // ScrollView'ı kaydırarak son eklenen veriye odaklanıyoruz
                            scrollViewRight.post {
                                scrollViewRight.fullScroll(ScrollView.FOCUS_DOWN)
                            }
                        }
                    }
                    // Geçerli zamanı güncelliyoruz
                    currentTime += timePerBuffer
                }

                // PitchProcessor (frekans tespiti işleyicisi) oluşturuluyor
                val pitchProcessor = PitchProcessor(
                    PitchProcessor.PitchEstimationAlgorithm.YIN, // YIN algoritması kullanılıyor
                    sampleRate, // Ses örnekleme hızı
                    bufferSize, // Veri bloğu büyüklüğü
                    pitchHandler // Frekans tespiti için handler
                )
                // Dispatcher'a pitchProcessor ekleniyor
                dispatcher.addAudioProcessor(pitchProcessor)

                // Ses verilerini işlemek için dispatcher'ı başlatıyoruz
                thread(start = true) {
                    dispatcher.run()
                }
            } else {
                // Dosya açılamıyorsa hata mesajı gösteriliyor
                runOnUiThread {
                    frequencyTextView.text = "Error: Dosya Uzantısı Açılamıyor"
                }
            }
        } catch (e: Exception) {
            // Hata durumunda hata mesajı UI'ye yazdırılıyor
            runOnUiThread {
                frequencyTextView.text = "Error: ${e.message}"
            }
        }
    }

}
