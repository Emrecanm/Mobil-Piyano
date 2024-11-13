package com.example.denemee

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SerbestCalActivity : AppCompatActivity() {

    private lateinit var horizontalScrollView: HorizontalScrollView
    private lateinit var seekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_serbest_cal)

        // Kenarlıkları ve durum çubuğunu hesaba katmak için padding ayarı
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Geri dönme butonu
        val button = findViewById<ImageButton>(R.id.back1Button)
        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val button2 = findViewById<ImageButton>(R.id.kayitlarButton)
        button2.setOnClickListener{
            val intent = Intent(this, kaydedilen_sarkilar::class.java)
            startActivity(intent)
        }

        // HorizontalScrollView ve SeekBar referansları
        horizontalScrollView = findViewById(R.id.horizontalScrollView)
        seekBar = findViewById(R.id.scrollbar)

        // HorizontalScrollView'in manuel kaydırma özelliğini devre dışı bırakma
        horizontalScrollView.setOnTouchListener { _, _ -> true }

        // ScrollView'un genişliğini aldıktan sonra SeekBar'ı ayarlıyoruz
        horizontalScrollView.viewTreeObserver.addOnGlobalLayoutListener {
            val scrollViewWidth = horizontalScrollView.getChildAt(0).width
            val maxScroll = scrollViewWidth - horizontalScrollView.width
            seekBar.max = maxScroll
        }

        // SeekBar değiştiğinde ScrollView'u kaydır
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    horizontalScrollView.scrollTo(progress, 0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Gerekirse bir şey yap
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Gerekirse bir şey yap
            }
        })

        // ScrollView kaydırıldığında SeekBar'ı güncelle
        horizontalScrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            seekBar.progress = scrollX
        }
    }
}
