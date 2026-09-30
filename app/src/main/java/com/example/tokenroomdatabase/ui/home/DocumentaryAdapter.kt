package com.example.tokenroomdatabase.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tokenroomdatabase.R
import com.example.tokenroomdatabase.data.model.Documentary
import com.example.tokenroomdatabase.databinding.ItemDocumentaryBinding

class DocumentaryAdapter(
    private val onFavoriteClick: (Documentary) -> Unit,
    private val onDocumentClick: (Documentary) -> Unit,
    private val onShareClick: (Documentary) -> Unit,
    private val onTextClick: (Documentary) -> Unit,
    private val onDeleteClick: (Documentary) -> Unit,
    private val isFavoriteScreen: Boolean = false
) : RecyclerView.Adapter<DocumentaryAdapter.DocumentaryViewHolder>() {

    private var documentaryList = emptyList<Documentary>()

    fun submitList(list: List<Documentary>) {
        val diffCallback = DocumentaryDiffCallback(documentaryList, list)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        documentaryList = list
        diffResult.dispatchUpdatesTo(this)
    }

    private class DocumentaryDiffCallback(
        private val oldList: List<Documentary>,
        private val newList: List<Documentary>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldItemPosition != newItemPosition) return false
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocumentaryViewHolder {
        val binding = ItemDocumentaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DocumentaryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DocumentaryViewHolder,
        position: Int
    ) {
        holder.bind(documentaryList[position], position)
    }

    override fun getItemCount(): Int = documentaryList.size

    override fun onViewRecycled(holder: DocumentaryViewHolder) {
        holder.releasePlayer()
        super.onViewRecycled(holder)
    }

    inner class DocumentaryViewHolder(
        private val binding: ItemDocumentaryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            documentary: Documentary,
            position: Int
        ) {
            // Set item position number
            binding.tvNumber.text = "${position + 1}"

            releasePlayer()

            // Dynamic height adjustment for Favorite screen vs Normal screen
            if (isFavoriteScreen) {
                binding.layoutMediaContainer.layoutParams.height =
                    (120 * binding.root.resources.displayMetrics.density).toInt()

                binding.tvDocumentaryDescription.layoutParams.height =
                    (120 * binding.root.resources.displayMetrics.density).toInt()
                binding.tvDocumentaryDescription.maxLines = 7
            } else {

                binding.tvDocumentaryDescription.layoutParams.height =
                    (90 * binding.root.resources.displayMetrics.density).toInt()
                binding.tvDocumentaryDescription.maxLines = 4
            }

            // USER NAME
            binding.tvDocumentaryUsername.text = documentary.username

            // PROFILE IMAGE
            if (documentary.userImageUri.isNotEmpty()) {
                try {
                    Glide.with(binding.root.context)
                        .load(Uri.parse(documentary.userImageUri))
                        .placeholder(R.drawable.person_icon)
                        .error(R.drawable.person_icon)
                        .circleCrop()
                        .into(binding.ivDocumentaryProfile)
                } catch (_: Exception) {
                    binding.ivDocumentaryProfile.setImageResource(R.drawable.person_icon)
                }
            } else {
                binding.ivDocumentaryProfile.setImageResource(R.drawable.person_icon)
            }

            // UNIFORM TEXT SIZES & TITLE FORMAT
            binding.tvDocumentaryUsername.textSize = 11f
            binding.tvDocumentaryTitle.textSize = 12f

            val formattedTitle = documentary.title.take(20)

            // HIDE ALL CONTENT FIRST
            binding.tvDocumentaryTitle.visibility = View.GONE
            binding.tvDocumentaryDescription.visibility = View.GONE
            binding.layoutMediaContainer.visibility = View.GONE
            binding.ivDocumentaryMedia.visibility = View.GONE
            binding.layoutVideo.visibility = View.GONE
            binding.videoDocumentary.visibility = View.GONE
            binding.layoutPdf.visibility = View.GONE

            // MEDIA TYPE
            when (documentary.mediaType) {
                "text" -> {
                    binding.tvDocumentaryTitle.visibility = View.VISIBLE
                    binding.tvDocumentaryTitle.text = formattedTitle

                    binding.tvDocumentaryDescription.visibility = View.VISIBLE
                    binding.tvDocumentaryDescription.text = documentary.description

                    binding.layoutMediaContainer.visibility = View.GONE
                }

                "image" -> {
                    binding.tvDocumentaryTitle.visibility = View.VISIBLE
                    binding.tvDocumentaryTitle.text = formattedTitle

                    binding.layoutMediaContainer.visibility = View.VISIBLE

                    if (documentary.documentUri.isNotEmpty()) {
                        binding.ivDocumentaryMedia.visibility = View.VISIBLE
                        try {
                            Glide.with(binding.root.context)
                                .load(Uri.parse(documentary.documentUri))
                                .centerCrop()
                                .into(binding.ivDocumentaryMedia)
                        } catch (_: Exception) {
                            binding.ivDocumentaryMedia.visibility = View.GONE
                        }
                    }
                }

                "video" -> {
                    binding.tvDocumentaryTitle.visibility = View.VISIBLE
                    binding.tvDocumentaryTitle.text = formattedTitle

                    binding.layoutMediaContainer.visibility = View.VISIBLE
                    binding.layoutVideo.visibility = View.VISIBLE

                    if (documentary.documentUri.isNotEmpty()) {
                        try {
                            Glide.with(binding.root.context)
                                .load(Uri.parse(documentary.documentUri))
                                .centerCrop()
                                .into(binding.ivVideoThumbnail)
                        } catch (_: Exception) {
                            binding.ivVideoThumbnail.setImageResource(R.drawable.camera)
                        }
                    } else {
                        binding.ivVideoThumbnail.setImageResource(R.drawable.camera)
                    }

                    binding.layoutVideo.setOnClickListener {
                        onDocumentClick(documentary)
                    }
                }

                "pdf" -> {
                    binding.tvDocumentaryTitle.visibility = View.VISIBLE
                    binding.tvDocumentaryTitle.text = formattedTitle

                    binding.layoutMediaContainer.visibility = View.VISIBLE
                    binding.layoutPdf.visibility = View.VISIBLE

                    binding.tvPdfName.text = formattedTitle
                }
            }

            // FAVORITE ICON
            binding.btnFavorite.clearColorFilter()
            if (documentary.isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.heart_filled)
            } else {
                binding.btnFavorite.setImageResource(R.drawable.heart_outline)
            }

            // ACTIONS
            binding.btnFavorite.setOnClickListener {
                onFavoriteClick(documentary)
            }

            binding.btnShare.setOnClickListener {
                onShareClick(documentary)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(documentary)
            }

            // CARD CLICK
            binding.root.setOnClickListener {
                if (documentary.mediaType == "text") {
                    onTextClick(documentary)
                } else {
                    onDocumentClick(documentary)
                }
            }
        }

        fun releasePlayer() {
            binding.videoDocumentary.player = null
        }
    }
}