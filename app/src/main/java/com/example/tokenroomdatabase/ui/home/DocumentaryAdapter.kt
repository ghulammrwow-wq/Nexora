package com.example.tokenroomdatabase.ui.home

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.example.tokenroomdatabase.R
import com.example.tokenroomdatabase.data.model.Documentary
import com.example.tokenroomdatabase.databinding.ItemDocumentaryBinding

class DocumentaryAdapter(
    private val onFavoriteClick: (Documentary) -> Unit,
    private val onDocumentClick: (Documentary) -> Unit,
    private val onShareClick: (Documentary) -> Unit,
    private val onTextClick: (Documentary) -> Unit,

    // ==================================================
    // NEW: DELETE CLICK
    // ==================================================

    private val onDeleteClick: (Documentary) -> Unit

) : RecyclerView.Adapter<DocumentaryAdapter.DocumentaryViewHolder>() {

    private var documentaryList =
        emptyList<Documentary>()

    fun submitList(
        list: List<Documentary>
    ) {

        documentaryList =
            list

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocumentaryViewHolder {

        val binding =
            ItemDocumentaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return DocumentaryViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: DocumentaryViewHolder,
        position: Int
    ) {

        holder.bind(
            documentaryList[position]
        )
    }

    override fun getItemCount(): Int =
        documentaryList.size

    override fun onViewRecycled(
        holder: DocumentaryViewHolder
    ) {

        holder.releasePlayer()

        super.onViewRecycled(
            holder
        )
    }

    inner class DocumentaryViewHolder(
        private val binding: ItemDocumentaryBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        private var player: ExoPlayer? = null

        fun bind(
            documentary: Documentary
        ) {

            releasePlayer()

            // ==================================================
            // USER NAME
            // ==================================================

            binding.tvDocumentaryUsername.text =
                documentary.username

            // ==================================================
            // PROFILE IMAGE
            // ==================================================

            if (
                documentary.userImageUri.isNotEmpty()
            ) {

                try {

                    binding.ivDocumentaryProfile.setImageURI(
                        Uri.parse(
                            documentary.userImageUri
                        )
                    )

                } catch (_: Exception) {

                    binding.ivDocumentaryProfile.setImageResource(
                        R.drawable.person_icon
                    )
                }

            } else {

                binding.ivDocumentaryProfile.setImageResource(
                    R.drawable.person_icon
                )
            }

            // ==================================================
            // DEFAULT TITLE SIZE
            // ==================================================

            binding.tvDocumentaryTitle.textSize =
                16f

            // ==================================================
            // HIDE ALL CONTENT FIRST
            // ==================================================

            binding.tvDocumentaryTitle.visibility =
                View.GONE

            binding.tvDocumentaryDescription.visibility =
                View.GONE

            binding.ivDocumentaryMedia.visibility =
                View.GONE

            binding.videoDocumentary.visibility =
                View.GONE

            binding.layoutPdf.visibility =
                View.GONE

            // ==================================================
            // MEDIA TYPE
            // ==================================================

            when (
                documentary.mediaType
            ) {

                // ==================================================
                // TEXT POST
                // ==================================================

                "text" -> {

                    binding.tvDocumentaryTitle.visibility =
                        View.VISIBLE

                    binding.tvDocumentaryTitle.text =
                        documentary.title

                    binding.tvDocumentaryDescription.visibility =
                        View.VISIBLE

                    binding.tvDocumentaryDescription.text =
                        documentary.description
                }

                // ==================================================
                // IMAGE POST
                // ==================================================

                "image" -> {

                    binding.tvDocumentaryTitle.visibility =
                        View.VISIBLE

                    binding.tvDocumentaryTitle.text =
                        documentary.title.take(25)

                    if (
                        documentary.documentUri.isNotEmpty()
                    ) {

                        binding.ivDocumentaryMedia.visibility =
                            View.VISIBLE

                        try {

                            binding.ivDocumentaryMedia.setImageURI(
                                Uri.parse(
                                    documentary.documentUri
                                )
                            )

                        } catch (_: Exception) {

                            binding.ivDocumentaryMedia.visibility =
                                View.GONE
                        }
                    }
                }

                // ==================================================
                // VIDEO POST
                // ==================================================

                "video" -> {

                    binding.tvDocumentaryTitle.visibility =
                        View.VISIBLE

                    binding.tvDocumentaryTitle.text =
                        documentary.title.take(25)

                    if (
                        documentary.documentUri.isNotEmpty()
                    ) {

                        binding.videoDocumentary.visibility =
                            View.VISIBLE

                        player =
                            ExoPlayer.Builder(
                                binding.root.context
                            ).build()

                        binding.videoDocumentary.player =
                            player

                        val mediaItem =
                            MediaItem.fromUri(
                                Uri.parse(
                                    documentary.documentUri
                                )
                            )

                        player?.setMediaItem(
                            mediaItem
                        )

                        player?.prepare()

                        player?.playWhenReady =
                            false
                    }
                }

                // ==================================================
                // PDF POST
                // ==================================================

                "pdf" -> {

                    binding.tvDocumentaryTitle.visibility =
                        View.VISIBLE

                    binding.tvDocumentaryTitle.text =
                        documentary.title.take(25)

                    binding.tvDocumentaryUsername.textSize =
                        10f

                    binding.tvDocumentaryTitle.textSize =
                        10f

                    binding.layoutPdf.visibility =
                        View.VISIBLE

                    binding.tvPdfName.text =
                        documentary.title
                }
            }

            // ==================================================
            // FAVORITE COLOR
            // ==================================================

            if (
                documentary.isFavorite
            ) {

                binding.btnFavorite.setColorFilter(
                    Color.RED
                )

            } else {

                binding.btnFavorite.setColorFilter(
                    Color.GRAY
                )
            }

            // ==================================================
            // FAVORITE CLICK
            // ==================================================

            binding.btnFavorite.setOnClickListener {

                onFavoriteClick(
                    documentary
                )
            }

            // ==================================================
            // SHARE CLICK
            // ==================================================

            binding.btnShare.setOnClickListener {

                onShareClick(
                    documentary
                )
            }

            // ==================================================
            // NEW: DELETE CLICK
            // ==================================================

            binding.btnDelete.setOnClickListener {

                onDeleteClick(
                    documentary
                )
            }

            // ==================================================
            // CARD CLICK
            // ==================================================

            binding.root.setOnClickListener {

                if (
                    documentary.mediaType == "text"
                ) {

                    onTextClick(
                        documentary
                    )

                } else {

                    onDocumentClick(
                        documentary
                    )
                }
            }
        }

        // ==================================================
        // RELEASE VIDEO PLAYER
        // ==================================================

        fun releasePlayer() {

            binding.videoDocumentary.player =
                null

            player?.release()

            player = null
        }
    }
}