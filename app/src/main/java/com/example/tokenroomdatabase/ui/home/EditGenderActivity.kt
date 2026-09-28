package com.example.tokenroomdatabase.ui.home

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tokenroomdatabase.R
import com.example.tokenroomdatabase.data.local.UserDatabase
import kotlinx.coroutines.launch

class EditGenderActivity : AppCompatActivity() {

    private lateinit var database: UserDatabase

    private lateinit var genderSelector: AutoCompleteTextView
    private lateinit var tvCancel: TextView
    private lateinit var tvSave: TextView

    private var userEmail = ""

    private val genders =
        arrayOf(
            "Select Gender",
            "Male",
            "Female"
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_gender)

        database =
            UserDatabase.getDatabase(this)

        userEmail =
            intent.getStringExtra("user_email")
                ?: ""

        genderSelector =
            findViewById(R.id.spGender)

        tvCancel =
            findViewById(R.id.tvCancel)

        tvSave =
            findViewById(R.id.tvSave)

        setupGenderSelector()

        loadGender()

        tvCancel.setOnClickListener {
            finish()
        }

        tvSave.setOnClickListener {
            saveGender()
        }
    }

    private fun setupGenderSelector() {

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genders
            )

        genderSelector.setAdapter(adapter)

        genderSelector.setOnClickListener {
            genderSelector.showDropDown()
        }
    }

    private fun loadGender() {

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)

            if (user == null) {
                finish()
                return@launch
            }

            genderSelector.setText(
                user.gender,
                false
            )
        }
    }

    private fun saveGender() {

        val newGender =
            genderSelector.text
                .toString()
                .trim()

        if (
            newGender == "Select Gender" ||
            newGender.isEmpty()
        ) {

            Toast.makeText(
                this,
                "Select gender",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(userEmail)
                    ?: return@launch

            val updatedUser =
                user.copy(
                    gender = newGender
                )

            database.userDao()
                .updateUser(updatedUser)

            Toast.makeText(
                this@EditGenderActivity,
                "Gender updated",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}