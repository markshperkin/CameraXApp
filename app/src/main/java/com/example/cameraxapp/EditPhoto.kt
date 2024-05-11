//options to make more sliders:
//1) sharpness
//2) temperature
//3) tint
//4) noise reduction
//5) vignette
//6) blur
package com.example.cameraxapp

import android.content.ContentValues
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EditPhoto : AppCompatActivity() {

    private lateinit var photoUri: Uri
    private lateinit var photoImageView: ImageView
    private lateinit var saveButton: Button
    private lateinit var brightnessSlider: SeekBar
    private lateinit var saturationSlider: SeekBar
    private lateinit var contrastSlider: SeekBar

    private var modifiedPhotoBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo)

        // Retrieve the selected photo URI from the intent
        val photoUriString = intent.getStringExtra("photo_uri")
        photoUri = Uri.parse(photoUriString)


        // Find the ImageView in the layout
        photoImageView = findViewById(R.id.photoImageView)

        // Find the sliders
        brightnessSlider = findViewById(R.id.brightnessSlider)
        saturationSlider = findViewById(R.id.saturationSlider)
        contrastSlider = findViewById(R.id.contrastSlider)


        // Set OnSeekBarChangeListener for the brightness slider
        brightnessSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the brightness of the bitmap
                val adjustedBitmap = modifyBrightness(modifiedPhotoBitmap!!, progress)
                // Update the ImageView with the adjusted bitmap
                photoImageView.setImageBitmap(adjustedBitmap)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Called when tracking of the brightness slider starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Called when tracking of the brightness slider stops
            }
        })

// Set OnSeekBarChangeListener for the saturation slider
        saturationSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the saturation of the bitmap
                val adjustedBitmap = modifySaturation(modifiedPhotoBitmap!!, progress)
                // Update the ImageView with the adjusted bitmap
                photoImageView.setImageBitmap(adjustedBitmap)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Called when tracking of the saturation slider starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Called when tracking of the saturation slider stops
            }
        })

        // Set OnSeekBarChangeListener for the contrast slider
        contrastSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the contrast of the bitmap
                val adjustedBitmap = modifyContrast(modifiedPhotoBitmap!!, progress)
                // Update the ImageView with the adjusted bitmap
                photoImageView.setImageBitmap(adjustedBitmap)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Called when tracking of the contrast slider starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Called when tracking of the contrast slider stops
            }
        })

        // Load the bitmap from the URI
        val inputStream = contentResolver.openInputStream(photoUri)
        modifiedPhotoBitmap = BitmapFactory.decodeStream(inputStream)

        // Set the bitmap to the ImageView
        photoImageView.setImageBitmap(modifiedPhotoBitmap)

        // Find the back button
        val backButton: Button = findViewById(R.id.backButton)

        // Set click listener for the back button
        backButton.setOnClickListener {
            // Define your navigation logic here
            showDiscardChangesDialog()
        }

        // Find the save button
        saveButton = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            // Get the bitmap from the ImageView
            val drawable = photoImageView.drawable
            val bitmap = (drawable as BitmapDrawable).bitmap

            // Call a method to save the edited photo
            saveModifiedPhoto(bitmap)
            // Navigate back to the main screen
            finish()
        }

    }

    // Function to modify the saturation of the bitmap
    private fun modifySaturation(bitmap: Bitmap, saturationLevel: Int): Bitmap {
        // Calculate the saturation adjustment based on the desired mapping
        val saturationFactor = saturationLevel / 50f

        // Create a new ColorMatrix with the saturation adjustment
        val colorMatrix = ColorMatrix().apply {
            setSaturation(saturationFactor)
        }

        // Create a new Paint object with the color filter
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        // Create a new bitmap with the adjusted saturation
        val adjustedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(adjustedBitmap)
        canvas.drawBitmap(bitmap, Matrix(), paint)

        return adjustedBitmap
    }

    // Function to modify the brightness of the bitmap
    private fun modifyBrightness(bitmap: Bitmap, brightnessLevel: Int): Bitmap {
        // Calculate the brightness adjustment based on the desired mapping
        val brightnessFactor = (brightnessLevel - 50) / 50f

        // Create a new ColorMatrix with the brightness adjustment
        val colorMatrix = ColorMatrix().apply {
            setScale(1f + brightnessFactor, 1f + brightnessFactor, 1f + brightnessFactor, 1f)
        }

        // Create a new Paint object with the color filter
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        // Create a new bitmap with the adjusted brightness
        val adjustedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(adjustedBitmap)
        canvas.drawBitmap(bitmap, Matrix(), paint)

        return adjustedBitmap
    }

    // Function to modify the contrast of the bitmap
    private fun modifyContrast(bitmap: Bitmap, contrastLevel: Int): Bitmap {
        // Calculate the contrast adjustment based on the desired mapping
        val contrastFactor = (contrastLevel - 50) / 50f * 255

        // Calculate the pivot point to preserve brightness
        val pivot = (255 / 2).toFloat()

        // Create a new ColorMatrix with the contrast adjustment
        val colorMatrix = ColorMatrix().apply {
            val scale = (contrastFactor + 255) / 255
            val translate = -0.5f * scale + 0.5f
            set(floatArrayOf(
                scale, 0f, 0f, 0f, translate * 255,
                0f, scale, 0f, 0f, translate * 255,
                0f, 0f, scale, 0f, translate * 255,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        // Create a new Paint object with the color filter
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        // Create a new bitmap with the adjusted contrast
        val adjustedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(adjustedBitmap)
        canvas.drawBitmap(bitmap, Matrix(), paint)

        return adjustedBitmap
    }


    private fun saveModifiedPhoto(modifiedPhotoBitmap: Bitmap) {

        // Create a time-stamped name for the modified photo
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_modified"

        // Define the content values for the new photo
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Get the content resolver
        val contentResolver = applicationContext.contentResolver

        // Define the URI for storing the modified photo
        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        // Save the modified photo to the specified URI
        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                modifiedPhotoBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            // If the device's API level is above Q, mark the photo as complete
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }

            Toast.makeText(this, "Modified photo saved to gallery", Toast.LENGTH_SHORT).show()
        } ?: run {
            Log.e(TAG, "Failed to create new image")
        }
    }

    override fun onBackPressed() {
        showDiscardChangesDialog()
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to discard changes?")
            .setPositiveButton("Discard") { dialogInterface: DialogInterface, i: Int ->
                // User clicked on Discard, navigate back to the main screen
                super.onBackPressed()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    companion object {
        private const val TAG = "EditPhotoActivity"
    }
}
