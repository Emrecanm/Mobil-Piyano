package com.example.denemee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.denemee.DBHelper

class sign_in : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            // Eğer e-posta ve şifre boş değilse
            if (email.isNotEmpty() && password.isNotEmpty()) {
                val dbHelper = DBHelper(this)

                // Veritabanında kullanıcıyı kontrol et
                if (dbHelper.checkUserCredentials(email, password)) {
                    Toast.makeText(this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()

                    val intent =
                        Intent(this, MainActivity::class.java)  // AnaSayfaActivity'e yönlendirme
                    startActivity(intent)
                    finish()
                } else {
                    // Hatalı giriş durumu
                    Toast.makeText(
                        this,
                        "Bilgilerinizi kontrol edin ve tekrar deneyin.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // E-posta veya şifre alanları boşsa
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}