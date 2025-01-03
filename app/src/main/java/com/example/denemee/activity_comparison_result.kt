package com.example.denemee

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class activity_comparison_result : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comparison_result)

        // Intent'ten gelen veriyi al
        val leftScrollText = intent.getStringExtra("leftContent")
        val rightScrollText = intent.getStringExtra("rightContent")

        // Metinleri satırlara ayır
        val leftLines = leftScrollText?.split("\n") ?: emptyList()
        val rightLines = rightScrollText?.split("\n") ?: emptyList()

        // LinearLayout içine her satırı ekleyeceğiz
        val linearLayout = findViewById<LinearLayout>(R.id.scrollViewContainer)

        // Benzerlik oranlarını tutacak liste
        val similarityList = mutableListOf<Int>()

        // Her iki içeriğin satırlarını sırayla yan yana ekle
        val maxLines = maxOf(leftLines.size, rightLines.size)
        for (i in 0 until maxLines) {
            val leftLine = leftLines.getOrNull(i)?.trim() ?: ""
            val rightLine = rightLines.getOrNull(i)?.trim() ?: ""

            // Eğer hem sol hem sağ satır boşsa, bu satırı geçelim
            if (leftLine.isEmpty() && rightLine.isEmpty()) {
                continue
            }

            // Sadece sayısal değer içeren satırları analiz et
            if (containsNumericValue(leftLine) && containsNumericValue(rightLine)) {
                // Sayısal benzerlik oranını hesapla
                val numericalSimilarity = calculateNumericalSimilarity(leftLine, rightLine)

                // Benzerlik oranını listeye ekle
                similarityList.add(numericalSimilarity)

                // Yatay kaydırma için HorizontalScrollView
                val scrollView = HorizontalScrollView(this)
                val row = LinearLayout(this)
                row.orientation = LinearLayout.HORIZONTAL
                row.setPadding(30, 0, 30, 10) // Satır arası boşluk ekleyelim

                // Sol TextView
                val leftTextView = TextView(this)
                leftTextView.text = leftLine
                leftTextView.setPadding(25, 15, 5, 15)
                leftTextView.setTextSize(18f)
                leftTextView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                leftTextView.ellipsize = null
                leftTextView.maxLines = 1 // Metin sadece bir satırda görünür

                // Sağ TextView (Vertical orientation)
                val rightTextView = TextView(this)
                rightTextView.text = rightLine
                rightTextView.setPadding(25, 15, 5, 15)
                rightTextView.setTextSize(18f)
                rightTextView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                rightTextView.ellipsize = null
                rightTextView.maxLines = 1 // Metin sadece bir satırda görünür

                val similarityColor = when (numericalSimilarity) {
                    in 80..100 -> getColor(android.R.color.holo_blue_dark)  // Yüksek benzerlik için yeşil
                    in 50..79 -> getColor(android.R.color.holo_orange_dark)  // Orta benzerlik için sarı
                    else -> getColor(android.R.color.holo_red_light)  // Düşük benzerlik için kırmızı
                }

                // Benzerlik Oranı TextView
                val similarityTextView = TextView(this)
                similarityTextView.text = "$numericalSimilarity%" // Sayısal benzerlik oranını gösteriyoruz
                similarityTextView.setTextColor(similarityColor)
                similarityTextView.setPadding(25, 15, 5, 15)
                similarityTextView.setTextSize(17f)
                similarityTextView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                // Row'a her TextView'i ekle
                row.addView(leftTextView)
                row.addView(rightTextView)
                row.addView(similarityTextView)

                // Yatay çizgi eklemek için bir View (Çizgi) ekliyoruz
                val lineView = View(this)
                val lineParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2) // Çizginin kalınlığı
                lineParams.setMargins(0, 10, 0, 10) // Çizgi ile metin arasına boşluk ekle
                lineView.layoutParams = lineParams
                lineView.setBackgroundColor(getColor(android.R.color.black)) // Çizginin rengi

                // Row'a çizgiyi ekle
                row.addView(lineView)

                // HorizontalScrollView içine LinearLayout ekle
                scrollView.addView(row)

                // Bu satırı LinearLayout'a ekle
                linearLayout.addView(scrollView)
            }
        }

        // Benzerlik oranlarını tek ve çift sırayla gruplama
        val oddSimilarityList = similarityList.filterIndexed { index, _ -> index % 2 == 0 }
        val evenSimilarityList = similarityList.filterIndexed { index, _ -> index % 2 != 0 }

        val oddAverage = if (oddSimilarityList.isNotEmpty()) oddSimilarityList.average().toInt() else 0

        val evenAverage = if (evenSimilarityList.isNotEmpty()) evenSimilarityList.average().toInt() else 0

        // Toplam benzerlik oranını hesapla
        val totalSimilarity = similarityList.average().toInt()

        // Sonuçları ekleyelim
        val resultRow = LinearLayout(this)
        resultRow.orientation = LinearLayout.HORIZONTAL
        resultRow.setPadding(0, 32, 0, 32)

        val oddAverageTextView = TextView(this)
        oddAverageTextView.text = "\n\n✓Sürelerin ortalama Benzerlik Oranı:  $oddAverage%"
        oddAverageTextView.setPadding(16, 8, 16, 8)
        oddAverageTextView.setTextSize(18f)
        linearLayout.addView(oddAverageTextView)

        val evenAverageTextView = TextView(this)
        evenAverageTextView.text = "✓Notaların Ortalama Benzerlik Oranı:  $evenAverage%"
        evenAverageTextView.setPadding(16, 8, 16, 8)
        evenAverageTextView.setTextSize(18f)
        linearLayout.addView(evenAverageTextView)

        val resultTextView = TextView(this)
        resultTextView.text = "✓Toplam Benzerlik Oranı:  $totalSimilarity%"
        resultTextView.setPadding(16, 8, 16, 8)
        resultTextView.setTextSize(18f)
        resultRow.addView(resultTextView)
        linearLayout.addView(resultRow)

        // Detaylı açıklamalar ekleyelim
        val detailedExplanationTextView = TextView(this)
        detailedExplanationTextView.text = """
            Sonuçlar, belirttiğiniz sürelerin ve notaların karşılaştırması ile elde edilmiştir. 
            Bu oranlar, benzerlik düzeyini gösterirken, şunları anlamanızı sağlar:

            1. **Sürelerin Ortalama Benzerlik Oranı ($oddAverage%)**:
                - Eğer bu oran yüksekse, sürelerinize ilişkin doğru ve uyumlu bir çakışma olduğunu söyleyebiliriz. 
                - Oran düşükse, notaların zamanlaması konusunda farklılıklar olabilir, belki de ritimde veya hızda küçük farklılıklar bulunmaktadır. 
                - Eğer süreler arasında büyük farklar varsa, müzik performansınızda eksiklikler olabilir veya belirli bir bölümü doğru bir şekilde zamanlamadığınız anlamına gelebilir.

            2. **Notaların Ortalama Benzerlik Oranı ($evenAverage%)**:
                - Bu oran, müzikteki belirli notaların doğru çalınıp çalınmadığını gösterir.
                - Yüksek oran, notaların doğru çalındığı ve diğer bölümlerle uyum sağladığını gösterir.
                - Düşük oran ise, belirli notaların yanlış çalındığını veya beklenen tonlama ve ton aralıklarında sapmalar yaşandığını işaret eder.

            3. **Toplam Benzerlik Oranı ($totalSimilarity%)**:
                - Bu oran, her iki metnin genel benzerliğini yansıtır. Eğer toplam oran yüksekse, genel olarak benzerlik yüksek demektir.
                - Eğer düşükse, metinlerin genel yapısında önemli farklılıklar olduğunu söyleyebiliriz. Bu, genellikle içeriklerin büyük ölçüde farklı olduğunu veya önemli bölümlerin atlandığını gösterebilir.

            **Uyarılar**:
            - Benzerlik oranları, tamamen matematiksel hesaplamalar olduğundan, bazen müzik performansındaki duygusal ve sanatsal yönleri yansıtmayabilir.
            - Eğer süreler çok düşükse, müzik temposunu yeniden gözden geçirebilirsiniz.
            - Notalar arasındaki uyumsuzluklar da çalma tekniğiyle ilgili eksiklikleri işaret edebilir, bu nedenle performansınızı geliştirmenizi tavsiye ederim.
        """.trimIndent()

        detailedExplanationTextView.setPadding(16, 16, 16, 16)
        detailedExplanationTextView.setTextSize(16f)
        detailedExplanationTextView.setTextColor(getColor(android.R.color.black))
        linearLayout.addView(detailedExplanationTextView)

        // Windows insets ile padding ekleme
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Sayısal değeri çeken fonksiyon
    fun extractNumber(text: String): Double {
        val regex = "\\d+(\\.\\d+)?".toRegex()  // Sayıyı bulmak için regex
        val matchResult = regex.find(text)  // Sayıyı bul
        return matchResult?.value?.toDouble() ?: 0.0  // Eğer sayı varsa döndür, yoksa 0 döndür
    }

    // Sayısal oranı hesaplama fonksiyonu
    fun calculateNumericalSimilarity(left: String, right: String): Int {
        val leftNumber = extractNumber(left)
        val rightNumber = extractNumber(right)

        // Eğer sağdaki sayı 0 ise oranı 0 olarak kabul et
        if (rightNumber == 0.0) {
            return 0
        }

        // Sayılar arasındaki farkı hesapla
        val difference = Math.abs(leftNumber - rightNumber)

        // Farkı 100 ile normalize et, yani fark küçükse benzerlik yüksek, büyükse düşük olacak
        val similarity = (1 - (difference / Math.max(leftNumber, rightNumber))) * 100

        // Benzerlik oranını 0 ile 100 arasında tut
        return similarity.toInt().coerceIn(0, 100)
    }

    // Sayısal değer içeren bir satır kontrol fonksiyonu
    fun containsNumericValue(text: String): Boolean {
        val regex = "\\d+(\\.\\d+)?".toRegex()  // Sayısal bir değer olup olmadığını kontrol et
        return regex.containsMatchIn(text)
    }
}
