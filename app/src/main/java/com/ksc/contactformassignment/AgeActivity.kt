package com.ksc.contactformassignment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AgeActivity : AppCompatActivity() {

    private lateinit var ageEditText: EditText
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_age)

        ageEditText = findViewById(R.id.edit_age)
        nextButton = findViewById(R.id.btn_next_age)

        nextButton.setOnClickListener {
            val ageInput = ageEditText.text.toString()
            if (ageInput.isBlank()) {
                Toast.makeText(this, "Please enter your age", Toast.LENGTH_SHORT).show()
            } else {
                val age = ageInput.toIntOrNull()
                if (age == null || age <= 0) {
                    Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
                } else {
                    // Pass data to the next activity
                    val intent = Intent(this, SelfieActivity::class.java)
                    intent.putExtra("userAge", age)
                    startActivity(intent)
                }
            }
        }
    }
}
