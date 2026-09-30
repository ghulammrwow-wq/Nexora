
package com.example.tokenroomdatabase.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tokenroomdatabase.R
import com.example.tokenroomdatabase.data.local.UserDatabase
import com.example.tokenroomdatabase.data.model.Documentary
import com.example.tokenroomdatabase.databinding.ActivityEditTextPostBinding
import kotlinx.coroutines.launch

class EditTextPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTextPostBinding
    private lateinit var database: UserDatabase

    private var documentaryId = 0
    private var documentary: Documentary? = null

    // Edit mode check
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditTextPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = UserDatabase.getDatabase(this)

        documentaryId = intent.getIntExtra("documentary_id", 0)

        loadPost()

        // Initially fields are NOT editable
        setEditMode(false)

        // Edit icon
        binding.btnEdit.setOnClickListener {
            setEditMode(true)

            // Cursor title field par aa jaye
            binding.etTitle.requestFocus()
            binding.etTitle.setSelection(binding.etTitle.text?.length ?: 0)
        }

        // Save
        binding.btnSave.setOnClickListener {
            if (isEditMode) {
                updatePost()
            }
        }

        // Back
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadPost() {

        lifecycleScope.launch {

            val post = database
                .documentaryDao()
                .getDocumentaryById(documentaryId)

            if (post == null) {

                Toast.makeText(
                    this@EditTextPostActivity,
                    "Post not found",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

                return@launch
            }

            documentary = post

            binding.etTitle.setText(post.title)
            binding.etDescription.setText(post.description)
        }
    }

    private fun setEditMode(editMode: Boolean) {

        isEditMode = editMode

        binding.etTitle.isEnabled = editMode
        binding.etDescription.isEnabled = editMode

        if (editMode) {

            // SAVE active
            binding.btnSave.setTextColor(
                getColor(R.color.blue)
            )

            // Edit icon hide
            binding.btnEdit.visibility = android.view.View.GONE

        } else {

            // SAVE disabled-looking
            binding.btnSave.setTextColor(
                android.graphics.Color.LTGRAY
            )

            // Edit icon visible
            binding.btnEdit.visibility = android.view.View.VISIBLE
        }
    }

    private fun updatePost() {

        val title = binding.etTitle.text
            .toString()
            .trim()

        val description = binding.etDescription.text
            .toString()
            .trim()

        // Title validation
        if (title.isEmpty()) {

            binding.etTitle.error = "Please enter title"

            return
        }

        // Description validation
        if (description.isEmpty()) {

            binding.etDescription.error =
                "Please enter description"

            return
        }

        lifecycleScope.launch {

            val oldPost = documentary

            if (oldPost == null) {

                Toast.makeText(
                    this@EditTextPostActivity,
                    "Post not found",
                    Toast.LENGTH_SHORT
                ).show()

                return@launch
            }

            // Updated object
            val updatedPost = oldPost.copy(
                title = title,
                description = description
            )

            // Update Room
            database
                .documentaryDao()
                .updateDocumentary(updatedPost)

            Toast.makeText(
                this@EditTextPostActivity,
                "Post updated successfully",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}

