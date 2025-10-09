package com.example.SmartSpender

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SecurityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        // Optional: Set up a back button if you want
        val ivBack = findViewById<ImageView>(R.id.ivBackSecurity)
        ivBack?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
