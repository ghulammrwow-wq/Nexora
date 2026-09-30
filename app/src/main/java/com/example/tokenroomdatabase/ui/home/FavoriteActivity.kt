package com.example.tokenroomdatabase.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tokenroomdatabase.data.local.UserDatabase
import com.example.tokenroomdatabase.data.model.Documentary
import com.example.tokenroomdatabase.databinding.ActivityFavoriteBinding
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class FavoriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteBinding

    private lateinit var database: UserDatabase

    private lateinit var adapter: DocumentaryAdapter


    // PAGINATION
    private var allFavorites =
        emptyList<Documentary>()

    private var visibleCount = 6

    private var isLoadingMore = false


    // TOUCH DETECTION
    private var startY = 0f

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

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

        setupRecyclerView()

        loadFavorites()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }


    // RECYCLER VIEW
    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecyclerView() {

        val layoutManager =
            GridLayoutManager(
                this,
                2
            )

        binding.recyclerView.layoutManager =
            layoutManager

        binding.recyclerView.itemAnimator = null


        // DOCUMENTARY ADAPTER
        adapter =
            DocumentaryAdapter(


                // FAVORITE
                onFavoriteClick = { documentary ->

                    lifecycleScope.launch {

                        database.documentaryDao()
                            .updateDocumentary(
                                documentary.copy(
                                    isFavorite = false,
                                    favoritedAt = 0L
                                )
                            )

                        Toast.makeText(
                            this@FavoriteActivity,
                            "Removed from favorite",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },


                // DOCUMENT
                onDocumentClick = { documentary ->

                    if (
                        documentary.mediaType == "video"
                    ) {

                        val intent =
                            Intent(
                                this,
                                FullScreenVideoActivity::class.java
                            )

                        intent.putExtra(
                            "video_uri",
                            documentary.documentUri
                        )

                        startActivity(
                            intent
                        )

                    } else {

                        openDocument(
                            documentary
                        )
                    }
                },


                // SHARE
                onShareClick = { documentary ->

                    shareDocumentary(
                        documentary
                    )
                },


                // TEXT
                onTextClick = { documentary ->

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


                // DELETE
                onDeleteClick = { documentary ->

                    showDeleteDialog(
                        documentary
                    )
                },

                isFavoriteScreen = true
            )

        binding.recyclerView.adapter =
            adapter


        // NORMAL SCROLL LISTENER
        binding.recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {

                    super.onScrolled(
                        recyclerView,
                        dx,
                        dy
                    )

                    if (dy <= 0) {
                        return
                    }

                    if (isLoadingMore) {
                        return
                    }

                    val lastVisibleItem =
                        layoutManager
                            .findLastVisibleItemPosition()

                    val totalItemCount =
                        layoutManager.itemCount

                    if (
                        lastVisibleItem >=
                        totalItemCount - 1
                    ) {

                        loadNextSix()
                    }
                }
            }
        )


        // UPWARD SWIPE DETECTION
        binding.recyclerView.setOnTouchListener { view, event ->

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    startY = event.rawY
                }

                MotionEvent.ACTION_UP -> {

                    val endY = event.rawY

                    val difference =
                        startY - endY

                    // User ne upward swipe kiya
                    if (difference > 50) {

                        // Agar aur favorites available hain
                        if (
                            visibleCount <
                            allFavorites.size
                        ) {

                            // Agar normal RecyclerView
                            // already bottom par hai
                            if (
                                !view.canScrollVertically(
                                    1
                                )
                            ) {

                                loadNextSix()
                            }
                        }
                    }
                }
            }

            false
        }
    }


    // LOAD FAVORITES FROM ROOM


    private fun loadFavorites() {

        lifecycleScope.launch {

            database.documentaryDao()
                .getFavoriteDocumentaries()
                .collect { list ->

                    allFavorites =
                        list


                    // DATABASE MEIN ITEMS KAM HO GAYE
                    if (
                        visibleCount >
                        allFavorites.size
                    ) {

                        visibleCount =
                            allFavorites.size
                    }


                    // INITIAL 6
                    if (
                        visibleCount == 0 &&
                        allFavorites.isNotEmpty()
                    ) {

                        visibleCount =
                            minOf(
                                6,
                                allFavorites.size
                            )
                    }


                    // DISPLAY ONLY VISIBLE ITEMS
                    adapter.submitList(
                        allFavorites.take(
                            visibleCount
                        )
                    )
                }
        }
    }


    // LOAD NEXT 6
    private fun loadNextSix() {

        if (isLoadingMore) {
            return
        }

        // Aur items nahi hain
        if (
            visibleCount >=
            allFavorites.size
        ) {
            return
        }

        isLoadingMore = true


        // SHOW LOADING
        binding.progressBar.visibility =
            View.VISIBLE


        // SMALL DELAY
        binding.recyclerView.postDelayed({


            // NEXT 6
            visibleCount += 6


            // REMAINING ITEMS
            if (
                visibleCount >
                allFavorites.size
            ) {

                visibleCount =
                    allFavorites.size
            }


            // UPDATE CARDS
            adapter.submitList(
                allFavorites.take(
                    visibleCount
                )
            )


            // HIDE LOADING
            binding.progressBar.visibility =
                View.GONE

            isLoadingMore = false

        }, 700)
    }


    // OPEN IMAGE / PDF
    private fun openDocument(
        documentary: Documentary
    ) {

        try {

            val uri =
                documentary.documentUri.toUri()

            val intent =
                Intent(
                    Intent.ACTION_VIEW
                )

            val mimeType =
                contentResolver.getType(
                    uri
                ) ?: when (
                    documentary.mediaType
                ) {

                    "image" ->
                        "image/*"

                    "pdf" ->
                        "application/pdf"

                    else ->
                        "*/*"
                }

            intent.setDataAndType(
                uri,
                mimeType
            )

            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            startActivity(
                intent
            )

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Cannot open file",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // SHARE DOCUMENTARY
    private fun shareDocumentary(
        documentary: Documentary
    ) {

        try {


            // TEXT POST
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


            // FILE POST
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
                documentary.documentUri.toUri()

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


    // DELETE CONFIRMATION
    private fun showDeleteDialog(
        documentary: Documentary
    ) {

        AlertDialog.Builder(
            this
        )
            .setTitle(
                "Delete Post"
            )
            .setMessage(
                "Are you sure you want to delete this post?"
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
                        .deleteDocumentaryById(
                            documentary.id
                        )

                    Toast.makeText(
                        this@FavoriteActivity,
                        "Post deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }
}