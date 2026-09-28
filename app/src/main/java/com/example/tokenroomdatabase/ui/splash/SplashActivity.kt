package com.example.tokenroomdatabase.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.tokenroomdatabase.databinding.ActivitySplashBinding
import com.example.tokenroomdatabase.ui.home.HomeActivity
import com.example.tokenroomdatabase.ui.login.LoginActivity
import com.example.tokenroomdatabase.utils.TokenManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ==========================================
        // NEXORA ANIMATION
        // ==========================================

        binding.tvNexora.alpha = 0f
        binding.tvNexora.scaleX = 0.7f
        binding.tvNexora.scaleY = 0.7f

        binding.tvNexora.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .start()


        // ==========================================
        // TAGLINE ANIMATION
        // ==========================================

        binding.tvTagline.alpha = 0f

        binding.tvTagline.postDelayed({

            binding.tvTagline.animate()
                .alpha(1f)
                .setDuration(700)
                .start()

        }, 700)


        // ==========================================
        // CHECK LOGIN SESSION
        // ==========================================

        Handler(Looper.getMainLooper()).postDelayed({

            val tokenManager = TokenManager(this)

            if (tokenManager.isSessionValid()) {

                val email = tokenManager.getUserEmail()

                startActivity(
                    Intent(
                        this,
                        HomeActivity::class.java
                    ).apply {
                        putExtra("user_email", email)
                    }
                )

            } else {

                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    )
                )
            }

            finish()

        }, 3000)
    }
}