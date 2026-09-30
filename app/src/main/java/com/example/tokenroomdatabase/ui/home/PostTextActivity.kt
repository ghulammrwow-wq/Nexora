package com.example.tokenroomdatabase.ui.home



import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tokenroomdatabase.data.local.UserDatabase
import com.example.tokenroomdatabase.data.model.Documentary
import com.example.tokenroomdatabase.databinding.ActivityPostTextBinding
import kotlinx.coroutines.launch

class PostTextActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostTextBinding
    private lateinit var database: UserDatabase

    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityPostTextBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        database = UserDatabase.getDatabase(this)

        userEmail =
            intent.getStringExtra("user_email") ?: ""

        setupDescriptionScroll()

        binding.btnPost.setOnClickListener {
            saveTextPost()
        }
    }

    private fun setupDescriptionScroll() {
        binding.etDescription.movementMethod = android.text.method.ScrollingMovementMethod()
        binding.etDescription.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and android.view.MotionEvent.ACTION_MASK) == android.view.MotionEvent.ACTION_UP) {
                v.parent.requestDisallowInterceptTouchEvent(false)
                v.performClick()
            }
            false
        }
    }

    private fun saveTextPost() {

        val title =
            binding.etTitle.text
                .toString()
                .trim()
                .take(20)

        val description =
            binding.etDescription.text
                .toString()
                .trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Please enter title"
            return
        }

        if (description.isEmpty()) {
            binding.etDescription.error =
                "Please enter description"
            return
        }

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)

            if (user == null) {

                Toast.makeText(
                    this@PostTextActivity,
                    "User not found",
                    Toast.LENGTH_SHORT
                ).show()

                return@launch
            }

            val documentary =
                Documentary(
                    title = title,
                    description = description,
                    documentUri = "",
                    mediaType = "text",
                    isFavorite = false,
                    userEmail = user.email,
                    username = user.name,
                    userImageUri = user.imageUri
                )

            database.documentaryDao()
                .insertDocumentary(documentary)

            Toast.makeText(
                this@PostTextActivity,
                "Text post uploaded successfully",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}