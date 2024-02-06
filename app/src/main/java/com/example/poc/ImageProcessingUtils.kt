package com.example.poc

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageProcessingUtils {

    fun resizeImageByDimensions(image: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun resizeImageByPercentage(image: Bitmap, percentage: Int): Bitmap {
        val newWidth = (image.width * percentage / 100.0).toInt()
        val newHeight = (image.height * percentage / 100.0).toInt()
        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true)
    }

    fun convertImageFormat(image: Bitmap, format: Bitmap.CompressFormat, quality: Int = 100): ByteArray {
        val outputStream = ByteArrayOutputStream()
        image.compress(format, quality, outputStream)
        return outputStream.toByteArray()
    }

    fun adjustImageToSize(image: Bitmap, targetSizeKb: Int, maxIterations: Int = 10): Bitmap {
        var quality = 95
        val step = 5
        var iteration = 0
        val outputStream = ByteArrayOutputStream()

        do {
            outputStream.reset()
            image.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val sizeKb = outputStream.size() / 1024

            if (sizeKb <= targetSizeKb * 1.05) { // Allowing a slight deviation
                break
            }

            quality -= step
            iteration++
        } while (iteration < maxIterations && quality > 0)

        return BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
    }

    fun saveImage(image: Bitmap, savePath: String, format: Bitmap.CompressFormat) {
        FileOutputStream(File(savePath)).use { out ->
            image.compress(format, 100, out) // PNG is a lossless format, the quality parameter is ignored.
        }
    }
}
