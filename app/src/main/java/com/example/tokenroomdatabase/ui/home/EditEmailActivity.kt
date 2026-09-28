package com.example.tokenroomdatabase.ui.home



import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tokenroomdatabase.R
import com.example.tokenroomdatabase.data.local.UserDatabase
import kotlinx.coroutines.launch

class EditEmailActivity : AppCompatActivity() {

    private lateinit var database: UserDatabase

    private lateinit var etEmail: EditText
    private lateinit var tvCancel: TextView
    private lateinit var tvSave: TextView

    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_email)

        database = UserDatabase.getDatabase(this)

        userEmail =
            intent.getStringExtra("user_email") ?: ""

        etEmail = findViewById(R.id.etEmail)
        tvCancel = findViewById(R.id.tvCancel)
        tvSave = findViewById(R.id.tvSave)

        loadEmail()

        tvCancel.setOnClickListener {
            finish()
        }

        tvSave.setOnClickListener {
            saveEmail()
        }
    }

    private fun loadEmail() {

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)

            if (user == null) {
                finish()
                return@launch
            }

            etEmail.setText(user.email)

            etEmail.setSelection(
                etEmail.text.length
            )
        }
    }

    private fun saveEmail() {

        val newEmail =
            etEmail.text
                .toString()
                .trim()

        if (
            !Patterns.EMAIL_ADDRESS
                .matcher(newEmail)
                .matches()
        ) {

            etEmail.error =
                "Enter valid email"

            return
        }

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)
                    ?: return@launch


            // Check duplicate email
            if (newEmail != user.email) {

                val existingUser =
                    database.userDao()
                        .getUserByEmail(newEmail)

                if (existingUser != null) {

                    etEmail.error =
                        "Email already registered"

                    return@launch
                }
            }


            val updatedUser =
                user.copy(
                    email = newEmail
                )


            database.userDao()
                .updateUser(updatedUser)


            // New email ko HomeActivity ko batane ke liye
            Toast.makeText(
                this@EditEmailActivity,
                "Email updated",
                Toast.LENGTH_SHORT
            ).show()


            finish()
        }
    }
}