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

class EditUsernameActivity : AppCompatActivity() {

    private lateinit var database: UserDatabase

    private lateinit var etUsername: EditText
    private lateinit var tvCancel: TextView
    private lateinit var tvSave: TextView

    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_username)

        database =
            UserDatabase.getDatabase(this)

        userEmail =
            intent.getStringExtra("user_email")
                ?: ""

        etUsername =
            findViewById(R.id.etUsername)

        tvCancel =
            findViewById(R.id.tvCancel)

        tvSave =
            findViewById(R.id.tvSave)

        loadUsername()

        tvCancel.setOnClickListener {
            finish()
        }

        tvSave.setOnClickListener {
            saveUsername()
        }
    }

    private fun loadUsername() {

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)

            if (user == null) {
                finish()
                return@launch
            }

            // Current username show hoga
            etUsername.setText(user.username)

            // Cursor end par
            etUsername.setSelection(
                etUsername.text.length
            )
        }
    }

    private fun saveUsername() {

        val newUsername =
            etUsername.text
                .toString()
                .trim()

        if (newUsername.isEmpty()) {

            etUsername.error =
                "Enter username"

            return
        }

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)

            if (user == null) {
                return@launch
            }

            val updatedUser =
                user.copy(
                    username = newUsername
                )

            database.userDao()
                .updateUser(updatedUser)

            Toast.makeText(
                this@EditUsernameActivity,
                "Username updated",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}