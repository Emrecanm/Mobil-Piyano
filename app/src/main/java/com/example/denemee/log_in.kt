package com.example.denemee

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.denemee.DBHelper

class log_in : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        val nameInput: EditText = findViewById(R.id.nameInput)
        val surnameInput: EditText = findViewById(R.id.surnameInput)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val registerButton: Button = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val name = nameInput.text.toString()
            val surname = surnameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (name.isNotEmpty() && surname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                val dbHelper = DBHelper(this)

                // E-posta adresi veritabanında var mı kontrol et
                if (dbHelper.isEmailExists(email)) {
                    Toast.makeText(this, "Bu e-posta adresi ile bir kullanıcı zaten var.", Toast.LENGTH_SHORT).show()
                } else {
                    // E-posta adresi veritabanında yoksa yeni kullanıcıyı ekle
                    dbHelper.addUser(name, surname, email, password)
                    Toast.makeText(this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()

                    // Kayıt başarılı olduktan sonra "Giriş Yap" ekranına yönlendir
                    val intent = Intent(this, sign_in::class.java)
                    startActivity(intent)
                    finish() // Bu aktiviteyi kapat
                }
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}