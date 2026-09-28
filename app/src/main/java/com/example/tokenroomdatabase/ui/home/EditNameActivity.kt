package com.example.tokenroomdatabase.ui.home

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tokenroomdatabase.R
import com.example.tokenroomdatabase.data.local.UserDatabase
import kotlinx.coroutines.launch

class EditNameActivity : AppCompatActivity() {

    private lateinit var database: UserDatabase

    private lateinit var etName: EditText
    private lateinit var tvCancel: TextView
    private lateinit var tvSave: TextView

    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_name)

        database =
            UserDatabase.getDatabase(this)

        userEmail =
            intent.getStringExtra("user_email") ?: ""

        etName =
            findViewById(R.id.etName)

        tvCancel =
            findViewById(R.id.tvCancel)

        tvSave =
            findViewById(R.id.tvSave)

        loadCurrentName()

        // Cancel
        tvCancel.setOnClickListener {
            finish()
        }

        // Save
        tvSave.setOnClickListener {
            saveName()
        }
    }

    private fun loadCurrentName() {

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)

            if (user == null) {

                finish()

                return@launch
            }

            etName.setText(
                user.name
            )

            etName.setSelection(
                etName.text.length
            )
        }
    }

    private fun saveName() {

        val newName =
            etName.text
                .toString()
                .trim()

        if (newName.isEmpty()) {

            etName.error =
                "Enter your name"

            return
        }

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)

            if (user == null) {

                Toast.makeText(
                    this@EditNameActivity,
                    "User not found",
                    Toast.LENGTH_SHORT
                ).show()

                return@launch
            }

            // Update User
            val updatedUser =
                user.copy(
                    name = newName
                )

            database.userDao()
                .updateUser(updatedUser)

            // Update all Documentary cards
            database.documentaryDao()
                .updateDocumentaryUserInfo(
                    email = user.email,
                    username = newName,
                    imageUri = user.imageUri
                )

            Toast.makeText(
                this@EditNameActivity,
                "Name updated",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}