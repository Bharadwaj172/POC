package com.example.poc



import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yalantis.ucrop.UCrop
import java.io.File

class CropActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No need to set a content view if UCrop is used directly.

        val sourceUri = intent.getParcelableExtra<Uri>("sourceUri")
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped.jpg"))

        sourceUri?.let {
            UCrop.of(it, destinationUri)
                .withAspectRatio(1f, 1f)
                .start(this)
        } ?: finish() // Finish activity if sourceUri is null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            setResult(Activity.RESULT_OK, Intent().setData(resultUri))
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            setResult(Activity.RESULT_CANCELED, Intent().putExtra("error", cropError.toString()))
        }
        finish()
    }
}