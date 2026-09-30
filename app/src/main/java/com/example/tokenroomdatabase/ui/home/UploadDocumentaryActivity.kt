package com.example.tokenroomdatabase.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.tokenroomdatabase.data.local.UserDatabase
import com.example.tokenroomdatabase.data.model.Documentary
import com.example.tokenroomdatabase.databinding.ActivityUploadDocumentaryBinding
import kotlinx.coroutines.launch

class UploadDocumentaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadDocumentaryBinding
    private lateinit var database: UserDatabase

    private var previewPlayer: ExoPlayer? = null
    private var selectedDocumentUri: Uri? = null
    private var userEmail = ""
    private var postType = "image"

    private val documentPicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                }

                selectedDocumentUri = uri

                binding.tvSelectedFile.text =
                    uri.lastPathSegment ?: "File selected"

                when (postType) {

                    // IMAGE
                    "image" -> {

                        binding.ivMediaPreview.setImageURI(uri)

                        binding.ivMediaPreview.visibility =
                            View.VISIBLE

                        binding.videoMediaPreview.visibility =
                            View.GONE

                        binding.layoutPdfPreview.visibility =
                            View.GONE

                        binding.ivAddIcon.visibility =
                            View.GONE

                        binding.tvMediaPicker.visibility =
                            View.GONE

                        binding.tvSelectedFile.visibility =
                            View.GONE
                    }


                    // VIDEO
                    "video" -> {

                        binding.ivMediaPreview.visibility =
                            View.GONE

                        binding.layoutPdfPreview.visibility =
                            View.GONE

                        binding.videoMediaPreview.visibility =
                            View.VISIBLE

                        binding.ivAddIcon.visibility =
                            View.GONE

                        binding.tvMediaPicker.visibility =
                            View.GONE

                        binding.tvSelectedFile.visibility =
                            View.GONE

                        previewPlayer?.release()

                        previewPlayer =
                            ExoPlayer.Builder(this)
                                .build()

                        binding.videoMediaPreview.player =
                            previewPlayer

                        val mediaItem =
                            MediaItem.fromUri(uri)

                        previewPlayer?.setMediaItem(mediaItem)

                        previewPlayer?.prepare()

                        previewPlayer?.playWhenReady = false
                    }

                    // PDF
                    "pdf" -> {

                        binding.ivMediaPreview.visibility =
                            View.GONE

                        binding.videoMediaPreview.visibility =
                            View.GONE

                        binding.layoutPdfPreview.visibility =
                            View.VISIBLE

                        binding.ivAddIcon.visibility =
                            View.GONE

                        binding.tvMediaPicker.visibility =
                            View.GONE

                        binding.tvSelectedFile.visibility =
                            View.GONE

                        binding.tvPdfPreviewName.text =
                            getFileName(uri)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityUploadDocumentaryBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        database =
            UserDatabase.getDatabase(this)

        userEmail =
            intent.getStringExtra("user_email")
                ?: ""

        postType =
            intent.getStringExtra("post_type")
                ?: "image"

        setupScreen()

        binding.mediaPickerContainer.setOnClickListener {
            openPicker()
        }

        binding.btnUpload.setOnClickListener {
            saveDocumentary()
        }
    }

    private fun setupScreen() {


        // DEFAULT STATE
        binding.ivMediaPreview.visibility =
            View.GONE

        binding.videoMediaPreview.visibility =
            View.GONE

        binding.layoutPdfPreview.visibility =
            View.GONE

        binding.ivAddIcon.visibility =
            View.VISIBLE

        binding.tvMediaPicker.visibility =
            View.VISIBLE

        binding.tvSelectedFile.visibility =
            View.GONE

        when (postType) {


            // IMAGE
            "image" -> {

                binding.tvUploadTitle.text =
                    "Post Image"

                binding.tvMediaPicker.text =
                    "Tap to select image"

                binding.tvSelectedFile.text =
                    "No image selected"
            }

            // VIDEO
            "video" -> {

                binding.tvUploadTitle.text =
                    "Post Video"

                binding.tvMediaPicker.text =
                    "Tap to select video"

                binding.tvSelectedFile.text =
                    "No video selected"
            }

            // PDF
            "pdf" -> {

                binding.tvUploadTitle.text =
                    "Post PDF"

                binding.tvMediaPicker.text =
                    "Tap to select PDF"

                binding.tvSelectedFile.text =
                    "No PDF selected"
            }
        }
    }

    private fun openPicker() {

        val mimeType =
            when (postType) {

                "image" ->
                    arrayOf("image/*")

                "video" ->
                    arrayOf("video/*")

                "pdf" ->
                    arrayOf("application/pdf")

                else ->
                    arrayOf("*/*")
            }

        documentPicker.launch(mimeType)
    }

    private fun getFileName(uri: Uri): String {

        var fileName = "Selected PDF"

        val cursor =
            contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )

        cursor?.use {

            val nameIndex =
                it.getColumnIndex(
                    android.provider.OpenableColumns.DISPLAY_NAME
                )

            if (nameIndex >= 0 && it.moveToFirst()) {

                fileName =
                    it.getString(nameIndex)
            }
        }

        return fileName
    }

    private fun saveDocumentary() {

        val title =
            binding.etTitle.text
                .toString()
                .trim()
                .take(20)

        val uri =
            selectedDocumentUri

        if (title.isEmpty()) {

            binding.etTitle.error =
                "Please enter title"

            return
        }

        if (uri == null) {

            Toast.makeText(
                this,
                "Please select a file",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)

            if (user == null) {

                Toast.makeText(
                    this@UploadDocumentaryActivity,
                    "User not found",
                    Toast.LENGTH_SHORT
                ).show()

                return@launch
            }

            val documentary =
                Documentary(
                    title = title,
                    description = "",
                    documentUri = uri.toString(),
                    mediaType = postType,
                    isFavorite = false,
                    userEmail = user.email,
                    username = user.name,
                    userImageUri = user.imageUri
                )

            database.documentaryDao()
                .insertDocumentary(documentary)

            Toast.makeText(
                this@UploadDocumentaryActivity,
                "Post uploaded successfully",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    override fun onDestroy() {

        previewPlayer?.release()
        previewPlayer = null

        super.onDestroy()
    }
}