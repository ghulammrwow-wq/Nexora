package com.example.tokenroomdatabase.ui.home

import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.tokenroomdatabase.databinding.ActivityFullScreenVideoBinding

@OptIn(UnstableApi::class)
class FullScreenVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenVideoBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFullScreenVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUri = intent.getStringExtra("video_uri")
        if (videoUri.isNullOrEmpty()) {
            finish()
            return
        }

        player = ExoPlayer.Builder(this).build()

        binding.fullScreenPlayer.player = player
        binding.fullScreenPlayer.controllerShowTimeoutMs = 1000
        binding.fullScreenPlayer.showController()

        val mediaItem = MediaItem.fromUri(Uri.parse(videoUri))
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    override fun onStop() {
        player?.pause()
        super.onStop()
    }

    override fun onDestroy() {
        binding.fullScreenPlayer.player = null
        player?.release()
        player = null
        super.onDestroy()
    }
}