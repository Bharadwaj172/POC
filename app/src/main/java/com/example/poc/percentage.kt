package com.example.poc

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class PercentageActivity : AppCompatActivity() {

    private lateinit var seekBarSize: SeekBar
    private lateinit var textViewPercentage: TextView
    private lateinit var buttonResizeImage: Button
    private var originalBitmap: Bitmap? = null
    private var currentPercentage: Int = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.percentage)

        // Initialize views
        seekBarSize = findViewById(R.id.seekBarSize)
        textViewPercentage = findViewById(R.id.textViewPercentage)
        buttonResizeImage = findViewById(R.id.btn)

        // Set initial text for percentage
        textViewPercentage.text = "$currentPercentage%"

        // Setup listener for SeekBar changes
        seekBarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentPercentage = progress
                textViewPercentage.text = "$currentPercentage%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Load the original image from the URI passed with the intent
        val imageUriString = intent.getStringExtra("capturedImage")
        imageUriString?.let {
            val imageUri = Uri.parse(it)
            Log.d("PercentageActivity", "Received Image URI: $imageUri")
            try {
                val imageStream = contentResolver.openInputStream(imageUri)
                originalBitmap = BitmapFactory.decodeStream(imageStream)
                imageStream?.close()
                if (originalBitmap == null) {
                    Log.e("PercentageActivity", "Bitmap could not be decoded.")
                    Toast.makeText(this, "Failed to decode image.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PercentageActivity", "Error loading image", e)
                Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Toast.makeText(this, "Image URI is null.", Toast.LENGTH_LONG).show()
        }

        // Set a click listener for the resize button
        buttonResizeImage.setOnClickListener {
            originalBitmap?.let { bitmap ->
                val resizedBitmap = resizeBitmap(bitmap, currentPercentage)
                val resizedImagePath = saveBitmapToFile(resizedBitmap)
                navigateToImageDisplayActivity(resizedImagePath)
            } ?: Toast.makeText(this, "Image not loaded correctly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmap(source: Bitmap, percentage: Int): Bitmap {
        val width = source.width
        val height = source.height
        val newWidth = width * percentage / 100
        val newHeight = height * percentage / 100
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val file = File(getExternalFilesDir(null), "resized_image_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return file.absolutePath
    }

    private fun navigateToImageDisplayActivity(resizedImagePath: String) {
        val intent = Intent(this, ImageDisplayActivity::class.java).apply {
            putExtra("resized_image_path", resizedImagePath)
        }
        startActivity(intent)
    }
}
