package com.example.poc


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ImageDisplayActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var tvResizedImageSize: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_display)

        imageView = findViewById(R.id.resizedImageView) // Make sure this ID matches your layout
        tvResizedImageSize = findViewById(R.id.tvResizedImageSize)

        // Get the resized image file path from the intent
        val resizedImagePath = intent.getStringExtra("resized_image_path")

        val btnSend: Button = findViewById(R.id.sendImageButton)
        btnSend.setOnClickListener {
            val emailIntent = Intent(this, EmailActivity::class.java).apply {
                // Pass the resized image path to EmailActivity
                putExtra("resized_image_path", resizedImagePath)
            }
            startActivity(emailIntent)
        }


        resizedImagePath?.let {
            val imgFile = File(it)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                imageView.setImageBitmap(bitmap)
                tvResizedImageSize.text = getFileSize(imgFile)
            } else {
                tvResizedImageSize.text = "Error: File not found"
            }
        } ?: run {
            tvResizedImageSize.text = "Error: Image path not received"
        }
    }

    private fun getFileSize(file: File): String {
        val sizeInBytes = file.length()
        val sizeInKB = sizeInBytes / 1024
        val sizeInMB = sizeInKB / 1024
        return when {
            sizeInMB > 1 -> "$sizeInMB MB"
            sizeInKB > 1 -> "$sizeInKB KB"
            else -> "$sizeInBytes Bytes"
        }
    }
}
