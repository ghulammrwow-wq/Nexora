package com.example.tokenroomdatabase.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tokenroomdatabase.R
import com.example.tokenroomdatabase.data.local.UserDatabase
import com.example.tokenroomdatabase.databinding.ActivityLoginBinding
import com.example.tokenroomdatabase.ui.home.HomeActivity
import com.example.tokenroomdatabase.ui.signup.SignupActivity
import com.example.tokenroomdatabase.utils.TokenManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: UserDatabase
    private lateinit var tokenManager: TokenManager


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        window.statusBarColor =
            getColor(R.color.white)

        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR


        binding =
            ActivityLoginBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )


        database =
            UserDatabase.getDatabase(
                this
            )


        tokenManager =
            TokenManager(
                this
            )



        // CHECK SAVED LOGIN
        if (
            tokenManager.isSessionValid()
        ) {

            val savedEmail =
                tokenManager.getUserEmail()


            if (
                !savedEmail.isNullOrEmpty()
            ) {

                startActivity(
                    Intent(
                        this,
                        HomeActivity::class.java
                    ).putExtra(
                        "user_email",
                        savedEmail
                    )
                )

                finish()

                return
            }
        }



        // LOGIN BUTTON
        binding.btnLogin.setOnClickListener {

            login()
        }


        // SIGN UP
        binding.tvSignup.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SignupActivity::class.java
                )
            )
        }


        // FORGOT PASSWORD
        binding.tvForgotPassword.setOnClickListener {

            forgotPassword()
        }
    }



    // LOGIN
    private fun login() {

        val email =
            binding.etEmail
                .text
                .toString()
                .trim()


        val password =
            binding.etPassword
                .text
                .toString()
                .trim()


        if (
            !android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()
        ) {

            binding.etEmail.error =
                "Enter valid email"

            return
        }


        if (
            password.length !in 6..20
        ) {

            binding.etPassword.error =
                "Password must be 6 to 20 characters"

            return
        }


        lifecycleScope.launch {

            val user =
                database.userDao()
                    .loginUser(
                        email,
                        password
                    )


            if (
                user == null
            ) {

                Toast.makeText(
                    this@LoginActivity,
                    "Invalid email or password",
                    Toast.LENGTH_SHORT
                ).show()

                return@launch
            }



            // SAVE LOGIN IN SHAREDPREFERENCES
            tokenManager.saveLogin(

                token =
                    "token_${System.currentTimeMillis()}",

                email =
                    user.email
            )


            // OPEN HOME
            startActivity(

                Intent(
                    this@LoginActivity,
                    HomeActivity::class.java
                ).putExtra(
                    "user_email",
                    user.email
                )
            )


            finish()
        }
    }



    // FORGOT PASSWORD
    private fun forgotPassword() {

        val view =
            layoutInflater.inflate(
                R.layout.dialog_forgot_password,
                null
            )


        val email =
            view.findViewById<android.widget.EditText>(
                R.id.etForgotEmail
            )


        val password =
            view.findViewById<android.widget.EditText>(
                R.id.etNewPassword
            )


        val button =
            view.findViewById<android.widget.Button>(
                R.id.btnUpdatePassword
            )


        val dialog =
            AlertDialog.Builder(this)
                .setTitle(
                    "Forgot Password"
                )
                .setView(view)
                .create()


        button.setOnClickListener {

            val userEmail =
                email.text
                    .toString()
                    .trim()


            val newPassword =
                password.text
                    .toString()
                    .trim()


            if (
                !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(userEmail)
                    .matches()
            ) {

                email.error =
                    "Enter valid email"

                return@setOnClickListener
            }


            if (
                !newPassword.matches(
                    Regex("\\d{6}")
                )
            ) {

                password.error =
                    "Password must be 6 digits"

                return@setOnClickListener
            }


            lifecycleScope.launch {

                val user =
                    database.userDao()
                        .getUserByEmail(
                            userEmail
                        )


                if (
                    user == null
                ) {

                    Toast.makeText(
                        this@LoginActivity,
                        "Email not found",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }


                database.userDao()
                    .updateUser(
                        user.copy(
                            password =
                                newPassword
                        )
                    )


                Toast.makeText(
                    this@LoginActivity,
                    "Password updated successfully",
                    Toast.LENGTH_SHORT
                ).show()


                dialog.dismiss()
            }
        }


        dialog.show()
    }
}