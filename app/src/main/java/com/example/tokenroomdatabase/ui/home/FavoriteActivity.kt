package com.example.tokenroomdatabase.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tokenroomdatabase.data.local.UserDatabase
import com.example.tokenroomdatabase.data.model.Documentary
import com.example.tokenroomdatabase.databinding.ActivityFavoriteBinding
import kotlinx.coroutines.launch

class FavoriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteBinding

    private lateinit var database: UserDatabase

    private lateinit var documentaryAdapter: DocumentaryAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityFavoriteBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        database =
            UserDatabase.getDatabase(
                this
            )

        setupFavorites()
    }

    // ==================================================
    // SETUP FAVORITES
    // ==================================================

    private fun setupFavorites() {

        documentaryAdapter =
            DocumentaryAdapter(

                // ==================================================
                // FAVORITE
                // ==================================================

                onFavoriteClick = {
                        documentary ->

                    lifecycleScope.launch {

                        database.documentaryDao()
                            .removeFromFavorite(
                                documentary.id
                            )

                        Toast.makeText(
                            this@FavoriteActivity,
                            "Removed from favorite",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },

                // ==================================================
                // OPEN DOCUMENT
                // ==================================================

                onDocumentClick = {
                        documentary ->

                    try {

                        val uri =
                            Uri.parse(
                                documentary.documentUri
                            )

                        val intent =
                            Intent(
                                Intent.ACTION_VIEW
                            )

                        intent.setDataAndType(
                            uri,
                            contentResolver.getType(
                                uri
                            ) ?: "*/*"
                        )

                        intent.addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                        startActivity(
                            intent
                        )

                    } catch (_: Exception) {

                        Toast.makeText(
                            this@FavoriteActivity,
                            "Cannot open documentary",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },

                // ==================================================
                // SHARE
                // ==================================================

                onShareClick = {
                        documentary ->

                    shareDocumentary(
                        documentary
                    )
                },

                // ==================================================
                // TEXT POST CLICK
                // ==================================================

                onTextClick = {
                        documentary ->

                    val intent =
                        Intent(
                            this,
                            EditTextPostActivity::class.java
                        )

                    intent.putExtra(
                        "documentary_id",
                        documentary.id
                    )

                    startActivity(
                        intent
                    )
                },

                // ==================================================
                // NEW: DELETE FROM FAVORITE ONLY
                // ==================================================

                onDeleteClick = {
                        documentary ->

                    showRemoveFavoriteDialog(
                        documentary
                    )
                }
            )

        binding.rvFavorites.adapter =
            documentaryAdapter

        binding.rvFavorites.layoutManager =
            LinearLayoutManager(
                this
            )

        lifecycleScope.launch {

            database.documentaryDao()
                .getFavoriteDocumentaries()
                .collect { list ->

                    documentaryAdapter.submitList(
                        list
                    )
                }
        }
    }

    // ==================================================
    // NEW: REMOVE FAVORITE CONFIRMATION
    // ==================================================

    private fun showRemoveFavoriteDialog(
        documentary: Documentary
    ) {

        AlertDialog.Builder(
            this
        )
            .setTitle(
                "Remove from Favorite"
            )
            .setMessage(
                "Do you want to remove this post from favorites?"
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                lifecycleScope.launch {

                    database.documentaryDao()
                        .removeFromFavorite(
                            documentary.id
                        )

                    Toast.makeText(
                        this@FavoriteActivity,
                        "Removed from favorite",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    // ==================================================
    // SHARE DOCUMENTARY
    // ==================================================

    private fun shareDocumentary(
        documentary: Documentary
    ) {

        try {

            // ==================================================
            // TEXT POST
            // ==================================================

            if (
                documentary.mediaType == "text"
            ) {

                val text =
                    "${documentary.title}\n\n${documentary.description}"

                val intent =
                    Intent(
                        Intent.ACTION_SEND
                    )

                intent.type =
                    "text/plain"

                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    text
                )

                startActivity(
                    Intent.createChooser(
                        intent,
                        "Share"
                    )
                )

                return
            }

            // ==================================================
            // IMAGE / VIDEO / PDF
            // ==================================================

            if (
                documentary.documentUri.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "No file available",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            val uri =
                Uri.parse(
                    documentary.documentUri
                )

            val mimeType =
                contentResolver.getType(
                    uri
                ) ?: when (
                    documentary.mediaType
                ) {

                    "image" ->
                        "image/*"

                    "video" ->
                        "video/*"

                    "pdf" ->
                        "application/pdf"

                    else ->
                        "*/*"
                }

            val intent =
                Intent(
                    Intent.ACTION_SEND
                )

            intent.type =
                mimeType

            intent.putExtra(
                Intent.EXTRA_STREAM,
                uri
            )

            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            startActivity(
                Intent.createChooser(
                    intent,
                    "Share"
                )
            )

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Cannot share documentary",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}