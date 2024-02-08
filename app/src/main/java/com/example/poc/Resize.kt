
package com.example.poc

//import ImageDisplayActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap.CompressFormat
import android.os.Environment
import android.text.format.Formatter.formatFileSize
import android.util.Log
import android.text.format.Formatter
import java.io.Serializable


class ResizeActivity : AppCompatActivity() {

    private lateinit var editTextWidth: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var spinnerUnits: Spinner
    private lateinit var buttonResizeImage: Button
    private lateinit var buttonAsPercentage: Button
    private lateinit var spinnerDPI: Spinner
    private lateinit var textViewDPI: TextView
    private lateinit var editTextTargetFileSize: EditText
    private lateinit var spinnerSaveImageAs: Spinner

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.resize) // Make sure this is the correct layout file

        editTextWidth = findViewById(R.id.editTextWidth)
        editTextHeight = findViewById(R.id.editTextHeight)
        spinnerUnits = findViewById(R.id.spinnerUnits)
        buttonResizeImage = findViewById(R.id.btnri)
        buttonAsPercentage = findViewById(R.id.btnAsPercentage)

        spinnerDPI = findViewById(R.id.spinnerDPI)
        textViewDPI = findViewById(R.id.textViewDPI)

        editTextTargetFileSize = findViewById(R.id.editTextTargetFileSize)
        spinnerSaveImageAs = findViewById(R.id.spinnerSaveImageAs)

//        var textInputLayoutTargetFileSize = findViewById(R.id.textInputLayoutTargetFileSize)

        val imageUri = Uri.parse(intent.getStringExtra("ImageUri"))

        val imageNew = imageUri.toString()




        // Initialize the Spinner for unit selection
        ArrayAdapter.createFromResource(
            this,
            R.array.unit_options, // The array of your units
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerUnits.adapter = adapter
        }




        val dpiOptions = resources.getStringArray(R.array.dpi_options)
        val adapterDPI = ArrayAdapter(this, android.R.layout.simple_spinner_item, dpiOptions)
        adapterDPI.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDPI.adapter = adapterDPI




        spinnerUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedUnit = parent.getItemAtPosition(position).toString()
                updateDpiSpinnerAndVisibility(selectedUnit)


                editTextWidth.visibility = View.VISIBLE
                editTextHeight.visibility = View.VISIBLE
                editTextTargetFileSize.visibility = View.VISIBLE
                spinnerSaveImageAs.visibility = View.VISIBLE



//                if (selectedUnit == "inch"|| selectedUnit == "CentiMeter" || selectedUnit == "MilliMeter") {
//                    // Show the DPI options
//                    spinnerDPI.visibility = View.VISIBLE
//                    textViewDPI.visibility = View.VISIBLE
//                } else {
//                    // Hide the DPI options
//                    spinnerDPI.visibility = View.GONE
//                    textViewDPI.visibility = View.GONE
//                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }


        buttonResizeImage.setOnClickListener {
            val width = editTextWidth.text.toString().toIntOrNull()
            val height = editTextHeight.text.toString().toIntOrNull()
            val unit = spinnerUnits.selectedItem.toString()


            if (width != null && height != null) {
                resizeImage(imageUri, width, height, unit)
            } else {
                Toast.makeText(this, "Please enter valid width and height.", Toast.LENGTH_SHORT).show()
            }
        }


        val buttonPercentage: Button = findViewById(R.id.btnAsPercentage)
        buttonPercentage.setOnClickListener {
            val intent = Intent(this, PercentageActivity::class.java)
            intent.putExtra("capturedImage",imageNew)
            startActivity(intent)
        }

    }

    private fun updateDpiSpinnerAndVisibility(unit: String) {
        val isDpiVisible = unit == "inch" || unit == "Centimeter" || unit == "Millimeter"
        spinnerDPI.visibility = if (isDpiVisible) View.VISIBLE else View.GONE
        textViewDPI.visibility = if (isDpiVisible) View.VISIBLE else View.GONE

        if (isDpiVisible) {
            val dpiArrayId = when (unit) {
                "inch" -> R.array.dpi_options
                "Centimeter" -> R.array.dpi_options
                "Millimeter" -> R.array.dpi_options
                else -> R.array.dpi_options // Handle default case
            }

            ArrayAdapter.createFromResource(
                this,
                dpiArrayId,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDPI.adapter = adapter
            }
        }
    }


    private fun resizeImage(imageUri: Uri, width: Int, height: Int, unit: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = contentResolver.openInputStream(imageUri) ?: return@launch
                val originalBitmap = BitmapFactory.decodeStream(inputStream)

                // Calculate the new width and height based on the unit
                val (newWidth, newHeight) = when (unit) {
                    "inch" -> Pair(
                        convertInchesToPixels(width, resources.displayMetrics.xdpi),
                        convertInchesToPixels(height, resources.displayMetrics.ydpi)
                    )
                    "cm" -> Pair(
                        convertCmToPixels(width, resources.displayMetrics.xdpi),
                        convertCmToPixels(height, resources.displayMetrics.ydpi)
                    )
                    "mm" -> Pair(
                        convertMmToPixels(width, resources.displayMetrics.xdpi),
                        convertMmToPixels(height, resources.displayMetrics.ydpi)
                    )
                    else -> Pair(width, height) // Assume pixels if no unit is selected
                }

                val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

                val kernel = floatArrayOf(
                    0f, -1f, 0f,
                    -1f, 5f, -1f,
                    0f, -1f, 0f
                )
                val sharpenedBitmap = applySharpeningFilter(resizedBitmap, kernel)

                // Save the resized bitmap to a temporary file
                val resizedFile = saveBitmapToFile(cacheDir, "resized_image.png", resizedBitmap)
//                val fileSizeString = formatFileSize(resizedFile)

                val targetFileSizeString = editTextTargetFileSize.text.toString()
                val targetFileSizeInKB = if (targetFileSizeString.isNotEmpty()) {
                    targetFileSizeString.toIntOrNull() ?: 0 // Default to 0 if input is not a valid integer
                } else {
                    null
                }

                val fileToSave: Any = if (targetFileSizeInKB != null) {
                    // Compress the image to attempt to reach the desired file size
                    compressImageToTargetFileSize(resizedBitmap, targetFileSizeInKB)
                } else {
                    // Save the bitmap without compression if no file size was specified
                    saveBitmapToFile(cacheDir, "resized_image.png", resizedBitmap)
                }




                withContext(Dispatchers.Main) {
                    // Create an intent to start ImageDisplayActivity with the resized image path
                    val intent = Intent(this@ResizeActivity, ImageDisplayActivity::class.java).apply {
                        putExtra("resized_image_path", resizedFile.absolutePath)
                    }
                    startActivity(intent)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ResizeActivity, "Error resizing image: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }




    private fun compressImageToTargetFileSize(bitmap: Bitmap, targetFileSizeInMB: Int) {
        val targetFileSizeInBytes = targetFileSizeInMB * 1024 * 1024 // Convert MB to Bytes
        var low = 0
        var high = 100
        var compressedFile: File

        while (low <= high) {
            val quality = (low + high) / 2
            compressedFile = File(cacheDir, "compressed_image.jpg")

            FileOutputStream(compressedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }

            if (compressedFile.length() < targetFileSizeInBytes) {
                low = quality + 1
            } else if (compressedFile.length() > targetFileSizeInBytes) {
                high = quality - 1
            } else {
                break
            }
        }
    }




    private fun applySharpeningFilter(bitmap: Bitmap, kernel: FloatArray) {
        // Implementation of the ConvolutionMatrix class's computeConvolution3x3 method
        // This method should apply the kernel to the bitmap and return the sharpened bitmap
        // ...
    }

    // Helper functions to convert units to pixels
    private fun convertInchesToPixels(inches: Int, dpi: Float): Int {
        return (inches * dpi).toInt()
    }

    private fun convertCmToPixels(cm: Int, dpi: Float): Int {
        return (cm * dpi / 2.54f).toInt()
    }

    private fun convertMmToPixels(mm: Int, dpi: Float): Int {
        return (mm * dpi / 25.4f).toInt()
    }

    private fun saveBitmapToFile(directory: File, filename: String, bitmap: Bitmap): File {
        val imageFile = File(directory, filename)
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // PNG is a lossless format, the compression factor (100) is ignored
        }
        return imageFile
    }


    private fun getFileSizeInKB(file: File): Int {
        val fileSizeInBytes = file.length()
        return (fileSizeInBytes / 1024).toInt()
    }
}