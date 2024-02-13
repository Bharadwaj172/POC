package com.example.poc


import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.os.Build
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import java.io.IOException
import java.util.Locale

class ImageDisplayActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var tvResizedImageSize: TextView
    private lateinit var spinner: Spinner
    private var newImagePath: String? = null
    private var imagePaths = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_display)

        imageView = findViewById(R.id.resizedImageView) // Make sure this ID matches your layout
        tvResizedImageSize = findViewById(R.id.tvResizedImageSize)


        val resizedImagePath = intent.getStringExtra("resized_image_path")

        spinner = findViewById(R.id.spinnerSaveImageA) // This is your dropdown (Spinner)
//        val btnSave: Button = findViewById(R.id.saveImageButton)
        val btnResize: Button = findViewById(R.id.convertResizeButton)
//        val btnSend: Button = findViewById(R.id.sendImageButton)

        val btnSave: Button = findViewById(R.id.saveImageButton)

        // Set up Spinner with formats
        val formats = arrayOf("PDF", "JPG", "PNG", "WEBP")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, formats)

        // Logic for resize button
        btnResize.setOnClickListener {
            resizedImagePath?.let { path ->
                val format = spinner.selectedItem.toString()
                val bitmap = BitmapFactory.decodeFile(path)
                newImagePath = when (format) {
                    "PDF" -> convertToPdf(bitmap, path)
                    "JPG" -> convertToImage(bitmap, path, Bitmap.CompressFormat.JPEG)
                    "PNG" -> convertToImage(bitmap, path, Bitmap.CompressFormat.PNG)
                    "WEBP" -> convertToImage(bitmap, path, Bitmap.CompressFormat.WEBP)
                    else -> path
                }
                Toast.makeText(this, "Image converted to $format", Toast.LENGTH_SHORT).show()
            }
        }


        btnSave.setOnClickListener {
            // Get the current path of the image displayed
            val currentImagePath = newImagePath ?: resizedImagePath

            currentImagePath?.let { path ->
                if (path.endsWith(".pdf")) {
                    // If the file is a PDF, save it using the specific PDF saving method
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        savePdfToDownloads(path)
                    } else {
                        // Handle saving for versions below Android Q if necessary
                        // You may need to request WRITE_EXTERNAL_STORAGE permission for this
                    }
                } else {
                    // If the file is an image, save it as usual
                    saveImageToGallery(path)
                }
            } ?: Toast.makeText(this, "No file to save", Toast.LENGTH_SHORT).show()
        }

//... existing code ...


//        btnSave.setOnClickListener {
//            newImagePath?.let { path ->
//                if (path.endsWith(".pdf")) {
//                    // If the file is a PDF, save it using the specific PDF saving method
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        savePdfToDownloads(path)
//                    } else {
//                        // Handle saving for versions below Android Q if necessary
//                        // You may need to request WRITE_EXTERNAL_STORAGE permission for this
//                    }
//                } else {
//                    // If the file is an image, save it as usual
//                    saveImageToGallery(path)
//                }
//            } ?: Toast.makeText(this, "No file to save", Toast.LENGTH_SHORT).show()
//        }


        // Get the resized image file path from the intent



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

    private fun saveImageToGallery(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            return
        }

        val mimeType = when (file.extension.toLowerCase(Locale.ROOT)) {
            "pdf" -> "application/pdf"
            else -> "image/jpeg" // Fallback for other formats
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        try {
            contentResolver.insert(MediaStore.Files.getContentUri("external"), values)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }
            }
            Toast.makeText(this, "File saved to gallery", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show()
        }
    }


    private fun convertToImage(bitmap: Bitmap, filePath: String, format: Bitmap.CompressFormat): String {
        // Extract the base name without extension
        val baseName = File(filePath).nameWithoutExtension

        // Determine the correct extension based on the format
        val extension = when (format) {
            Bitmap.CompressFormat.JPEG -> "jpg"
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.WEBP -> "webp"
            else -> "jpg"
        }

        // Create a new file name with the correct extension
        val newFileName = "$baseName.$extension"
        val newFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), newFileName)

        FileOutputStream(newFile).use { out ->
            bitmap.compress(format, 100, out)
        }

        return newFile.absolutePath
    }


    private fun convertToPdf(bitmap: Bitmap, path: String): String {
        val pdfDir = File(getExternalFilesDir(null), "pdfs")
        if (!pdfDir.exists()) pdfDir.mkdir()

        val pdfFile = File(pdfDir, "image_${System.currentTimeMillis()}.pdf")
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)

        try {
            pdfDocument.writeTo(FileOutputStream(pdfFile))
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        } finally {
            pdfDocument.close()
        }

        newImagePath = pdfFile.absolutePath
        return pdfFile.absolutePath
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun savePdfToDownloads(pdfPath: String) {
        val file = File(pdfPath)
        if (!file.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the file's name and MIME type.
        val fileName = file.name
        val mimeType = "application/pdf"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.SIZE, file.length())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        // Insert the metadata to the MediaStore.
        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

        try {
            // Write the PDF content to the MediaStore.
            contentResolver.openOutputStream(uri!!).use { outputStream ->
                FileInputStream(file).use { inputStream ->
                    inputStream.copyTo(outputStream!!, DEFAULT_BUFFER_SIZE)
                }
            }

            // Inform the media scanner about the new file so that it is immediately available to the user.
            MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null) { path, uri ->
                Log.d("PDF Save", "Scanned $path:")
                Log.d("PDF Save", "-> Uri = $uri")
            }

            // If running on Android Q and above, update the IS_PENDING status to false.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(uri, values, null, null)
            }

            Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show()
        }
    }


//    private fun createPdfFromImages(imagePaths: List<String>, outputFile: File) {
//        val pdfDocument = PdfDocument()
//
//        for (path in imagePaths) {
//            val bitmap = BitmapFactory.decodeFile(path)
//            // Assuming you want A4 size pages
//            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
//            val page = pdfDocument.startPage(pageInfo)
//
//            // Scale the bitmap to fit the PDF page
//            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, pageInfo.pageWidth, pageInfo.pageHeight, true)
//            val canvas = page.canvas
//            canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
//
//            if (!scaledBitmap.isRecycled) {
//                scaledBitmap.recycle()
//            }
//
//            pdfDocument.finishPage(page)
//        }
//
//        try {
//            pdfDocument.writeTo(FileOutputStream(outputFile))
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } finally {
//            pdfDocument.close()
//        }
//    }



}



