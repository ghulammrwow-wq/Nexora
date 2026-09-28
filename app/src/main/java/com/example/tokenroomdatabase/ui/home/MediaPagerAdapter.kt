package com.example.tokenroomdatabase.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tokenroomdatabase.data.model.Documentary
import com.example.tokenroomdatabase.databinding.ItemMediaPageBinding

class MediaPagerAdapter(
    private val onFavoriteClick: (Documentary) -> Unit,
    private val onDocumentClick: (Documentary) -> Unit,
    private val onShareClick: (Documentary) -> Unit,
    private val onTextClick: (Documentary) -> Unit,

    // ==================================================
    // NEW: DELETE CALLBACK
    // ==================================================

    private val onDeleteClick: (Documentary) -> Unit

) : RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder>() {

    private val pageTypes =
        listOf(
            "image",
            "video",
            "pdf"
        )

    private val pageLists =
        mutableListOf(
            emptyList<Documentary>(),
            emptyList<Documentary>(),
            emptyList<Documentary>()
        )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MediaViewHolder {

        val binding =
            ItemMediaPageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return MediaViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: MediaViewHolder,
        position: Int
    ) {

        val spanCount =
            if (
                pageTypes[position] == "pdf"
            ) {
                2
            } else {
                1
            }

        holder.binding.rvMedia.layoutManager =
            GridLayoutManager(
                holder.binding.root.context,
                spanCount
            )

        holder.bind(
            pageLists[position]
        )
    }

    override fun getItemCount(): Int =
        pageTypes.size

    fun updatePage(
        type: String,
        list: List<Documentary>
    ) {

        val position =
            pageTypes.indexOf(
                type
            )

        if (
            position == -1
        ) {
            return
        }

        pageLists[position] =
            list

        notifyItemChanged(
            position
        )
    }

    inner class MediaViewHolder(
        val binding: ItemMediaPageBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        private val adapter =
            DocumentaryAdapter(

                // FAVORITE
                onFavoriteClick =
                    onFavoriteClick,

                // DOCUMENT
                onDocumentClick =
                    onDocumentClick,

                // SHARE
                onShareClick =
                    onShareClick,

                // TEXT
                onTextClick =
                    onTextClick,

                // DELETE
                onDeleteClick =
                    onDeleteClick
            )

        init {

            binding.rvMedia.adapter =
                adapter
        }

        fun bind(
            list: List<Documentary>
        ) {

            adapter.submitList(
                list
            )
        }
    }
}