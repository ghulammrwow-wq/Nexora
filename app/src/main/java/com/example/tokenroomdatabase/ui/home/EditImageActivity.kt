package com.example.tokenroomdatabase.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tokenroomdatabase.R
import com.example.tokenroomdatabase.data.local.UserDatabase
import kotlinx.coroutines.launch

class EditImageActivity : AppCompatActivity() {

    private lateinit var database: UserDatabase

    private lateinit var imageView: ImageView
    private lateinit var tvCancel: TextView
    private lateinit var tvSave: TextView

    private var userEmail = ""

    private var selectedImageUri: Uri? = null

    private val gallery =
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

                selectedImageUri = uri

                imageView.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_edit_image
        )

        database =
            UserDatabase.getDatabase(this)

        userEmail =
            intent.getStringExtra("user_email")
                ?: ""

        imageView =
            findViewById(R.id.ivProfile)

        tvCancel =
            findViewById(R.id.tvCancel)

        tvSave =
            findViewById(R.id.tvSave)

        loadCurrentImage()

        // Image click → Gallery
        imageView.setOnClickListener {

            gallery.launch(
                arrayOf("image/*")
            )
        }

        // Cancel
        tvCancel.setOnClickListener {
            finish()
        }

        // Save
        tvSave.setOnClickListener {
            saveImage()
        }
    }

    private fun loadCurrentImage() {

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)

            if (user == null) {

                finish()

                return@launch
            }

            if (user.imageUri.isNotEmpty()) {

                try {

                    imageView.setImageURI(
                        Uri.parse(
                            user.imageUri
                        )
                    )

                } catch (_: SecurityException) {

                    imageView.setImageResource(
                        android.R.drawable.ic_menu_gallery
                    )
                }

            } else {

                imageView.setImageResource(
                    android.R.drawable.ic_menu_gallery
                )
            }
        }
    }

    private fun saveImage() {

        if (selectedImageUri == null) {

            Toast.makeText(
                this,
                "Select an image first",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)

            if (user == null) {

                finish()

                return@launch
            }

            val newImageUri =
                selectedImageUri.toString()

            // Update User
            val updatedUser =
                user.copy(
                    imageUri = newImageUri
                )

            database.userDao()
                .updateUser(updatedUser)

            // Update all Documentary cards
            database.documentaryDao()
                .updateDocumentaryUserInfo(
                    email = user.email,
                    username = user.name,
                    imageUri = newImageUri
                )

            Toast.makeText(
                this@EditImageActivity,
                "Profile image updated",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}