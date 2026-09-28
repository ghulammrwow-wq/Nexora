package com.example.tokenroomdatabase.ui.signup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tokenroomdatabase.R
import com.example.tokenroomdatabase.data.local.UserDatabase
import com.example.tokenroomdatabase.data.model.User
import com.example.tokenroomdatabase.databinding.ActivitySignupBinding
import com.example.tokenroomdatabase.ui.login.LoginActivity
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var database: UserDatabase
    private var imageUri: Uri? = null

    private val gallery =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                imageUri = uri
                binding.ivProfile.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.ivProfile.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.statusBarColor = getColor(R.color.white)


        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR



        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = UserDatabase.getDatabase(this)


        val genderList = arrayOf("Select Gender", "Male", "Female")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            genderList
        )
        binding.spGender.setAdapter(adapter)

        binding.ivProfile.setOnClickListener {
            gallery.launch(arrayOf("image/*"))
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnSignup.setOnClickListener {
            signup()
        }
    }

    private fun signup() {

        val username = binding.etUsername.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val gender = binding.spGender.text.toString()
        if (imageUri == null) {
            Toast.makeText(
                this,
                "Please select profile image",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (username.isEmpty()) {
            binding.etUsername.error = "Enter username"
            return
        }

        if (name.isEmpty()) {
            binding.etName.error = "Enter name"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter valid email"
            return
        }
        if (password.length !in 6..20) {
            binding.etPassword.error = "Password must be 6 to 20 characters"
            return
        }

        if (gender == "Select Gender") {
            Toast.makeText(
                this,
                "Select gender",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {

            if (database.userDao().getUserByEmail(email) != null) {
                binding.etEmail.error = "Email already registered"
                return@launch
            }

            database.userDao().insertUser(
                User(
                    username = username,
                    name = name,
                    email = email,
                    password = password,
                    gender = gender,
                    imageUri = imageUri.toString()
                )
            )

            Toast.makeText(
                this@SignupActivity,
                "Account created successfully",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(
                Intent(this@SignupActivity, LoginActivity::class.java)
            )

            finish()
        }
    }
}
