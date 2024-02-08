package com.example.poc


import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.poc.databinding.CameraBinding
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException


class CameraActivity : AppCompatActivity() {
    private lateinit var binding: CameraBinding
    private lateinit var capturedImageUri: Uri

    private lateinit var imageView: ImageView
    private lateinit var btnFlipRotate: Button
    private lateinit var btnCrop: Button

    private lateinit var tvImageSize: TextView
    private lateinit var tvFileSize: TextView


    private var isFlipped = false
    private var rotationAngle = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
// Retrieve image URI from intent
        capturedImageUri = Uri.parse(intent.getStringExtra("ImageUri"))

        imageView = findViewById(R.id.imageView)
        btnFlipRotate = findViewById(R.id.btnFlipRotate)
        btnCrop = findViewById(R.id.btnCrop)

        tvImageSize = findViewById(R.id.tvImageSize)
        tvFileSize = findViewById(R.id.tvFileSize)

        // Retrieve image URI from intent
        capturedImageUri = Uri.parse(intent.getStringExtra("ImageUri"))

//        val intentToPercentage = Intent(this, PercentageActivity::class.java).apply {
//            putExtra(
//                "ImageUri",
//                capturedImageUri.toString()
//            ) // Ensure capturedImageUri is not null and has a value
//        }

//        startActivity(intentToPercentage)

//        val intentToResize = Intent(this, ResizeActivity::class.java).apply {
//            putExtra(
//                "ImageUri",
//                capturedImageUri.toString()
//            ) // Ensure capturedImageUri is not null and has a value
//        }
//
//        startActivity(intentToResize)


        if (capturedImageUri != null) {
// Display the image in ImageView
            binding.imageView.setImageURI(capturedImageUri)
            binding.imageView.visibility = View.VISIBLE

            val btnSend: Button = findViewById(R.id.btn)
            btnSend.setOnClickListener {
                // Intent to navigate to EmailActivity
                val emailIntent = Intent(this, EmailActivity::class.java)
                startActivity(emailIntent)
            }


            val imageSize = getImageSize(capturedImageUri)
            imageSize?.let {
                // Update the TextView to show image size
                tvImageSize.text = getString(R.string.image_size_format, it.first, it.second)
            }

            val fileSize = getFileSize(capturedImageUri)
            tvFileSize.text = getString(R.string.file_size_format, fileSize)
        } else {
            Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show()
        }
        setupButtons()
    }




    private fun getFileSize(uri: Uri): String {
        val file = File(uri.path)
        val sizeInBytes = file.length()
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> "${sizeInBytes / 1024} KB"
            else -> "${sizeInBytes / (1024 * 1024)} MB"
        }
    }


    private fun getImageSize(uri: Uri): Pair<Int, Int>? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds =
                        true // Avoids memory allocation, returning null for the bitmap object but setting outWidth, outHeight and outMimeType
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                Pair(options.outWidth, options.outHeight)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

    }


    private fun setupButtons() {
        val btnResize: Button = findViewById(R.id.btnResize)
        btnResize.setOnClickListener {
            val intent = Intent(this, ResizeActivity::class.java).apply {
                putExtra("ImageUri", capturedImageUri.toString())
            }
            startActivity(intent)
        }

        // Inside CameraActivity's setupButtons function
//        val btnPercentage: Button = findViewById(R.id.btnAsPercentage) // Use the actual ID of your button
//        btnPercentage.setOnClickListener {
//            val intent = Intent(this, PercentageActivity::class.java).apply {
//                putExtra("ImageUri", capturedImageUri.toString())
//            }
//            startActivity(intent)
//        }


        // Inside CameraActivity's setupButtons function
//        val btnPercentage: Button = findViewById(R.id.btnAsPercentage) // Use the actual ID of your button
//        btnPercentage.setOnClickListener {
//            val intent = Intent(this, PercentageActivity::class.java).apply {
//                putExtra("ImageUri", capturedImageUri.toString())
//            }
//            startActivity(intent)
//        }


        btnFlipRotate.setOnClickListener {
            handleFlipRotateClick()
        }
        btnCrop.setOnClickListener {
            val cropIntent = Intent(this, CropActivity::class.java).apply {
                putExtra("sourceUri", capturedImageUri)
            }
            startActivityForResult(cropIntent, UCrop.REQUEST_CROP)
        }

    }

    private fun handleFlipRotateClick() {
        // Update rotationAngle for each click
        rotationAngle += 90f

        // Flip image horizontally if isFlipped is true
        if (isFlipped) {
            imageView.scaleX = -1f
        } else {
            imageView.scaleX = 1f
        }

        // Rotate the image based on the rotationAngle
        imageView.rotation = rotationAngle

        // Toggle the isFlipped value for the next click
        isFlipped = !isFlipped
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = data?.data
            if (resultUri != null) {
                // Update the ImageView with the cropped image
                imageView.setImageURI(resultUri)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(this, "Cropping failed: ${cropError?.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
