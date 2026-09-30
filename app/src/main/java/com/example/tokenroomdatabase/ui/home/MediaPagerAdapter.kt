package com.example.tokenroomdatabase.ui.home

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tokenroomdatabase.data.model.Documentary

class MediaPagerAdapter(
    private val onFavoriteClick: (Documentary) -> Unit,
    private val onDocumentClick: (Documentary) -> Unit,
    private val onShareClick: (Documentary) -> Unit,
    private val onTextClick: (Documentary) -> Unit,
    private val onDeleteClick: (Documentary) -> Unit
) : RecyclerView.Adapter<MediaPagerAdapter.PageViewHolder>() {

    private val pageTypes = listOf("image", "video", "pdf")

    private val pageLists = mutableMapOf(
        "image" to emptyList<Documentary>(),
        "video" to emptyList<Documentary>(),
        "pdf" to emptyList<Documentary>()
    )

    private val pageLoading = mutableMapOf(
        "image" to true,
        "video" to true,
        "pdf" to true
    )

    private val pageVisibleCounts = mutableMapOf(
        "image" to 6,
        "video" to 6,
        "pdf" to 6
    )

    private val activeHolders = mutableMapOf<String, PageViewHolder>()

    fun resetPage(mediaType: String) {
        pageVisibleCounts[mediaType] = 6
        activeHolders[mediaType]?.resetToFirst()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PageViewHolder {

        val frameLayout = FrameLayout(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val recyclerView = RecyclerView(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(4, 4, 4, 4)
            clipToPadding = false
            setHasFixedSize(false)
            layoutManager = GridLayoutManager(parent.context, 2)
            itemAnimator = null
        }

        // Center progress bar (sirf pehli baar room database se loading ke liye)
        val progressBar = ProgressBar(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(150, 150).apply {
                gravity = Gravity.CENTER
            }
        }


        val bottomProgressBar = ProgressBar(parent.context).apply {
            layoutParams = FrameLayout.LayoutParams(150, 150).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = 20
            }
            visibility = View.GONE
        }

        frameLayout.addView(recyclerView)
        frameLayout.addView(progressBar)
        frameLayout.addView(bottomProgressBar)

        return PageViewHolder(
            frameLayout,
            recyclerView,
            progressBar,
            bottomProgressBar
        )
    }

    override fun onBindViewHolder(
        holder: PageViewHolder,
        position: Int
    ) {
        val mediaType = pageTypes[position]
        activeHolders[mediaType] = holder
        holder.bind(mediaType)
    }

    override fun onViewRecycled(holder: PageViewHolder) {
        super.onViewRecycled(holder)
        val mediaType = holder.currentMediaType
        if (mediaType != null) {
            activeHolders.remove(mediaType)
        }
    }

    override fun getItemCount(): Int = pageTypes.size

    fun updatePage(
        mediaType: String,
        list: List<Documentary>
    ) {
        pageLists[mediaType] = list
        pageLoading[mediaType] = false
        activeHolders[mediaType]?.updateContent()
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class PageViewHolder(
        root: View,
        private val recyclerView: RecyclerView,
        private val progressBar: ProgressBar,
        private val bottomProgressBar: ProgressBar
    ) : RecyclerView.ViewHolder(root) {

        var currentMediaType: String? = null
            private set

        private var isLoadingMore = false
        private var startY = 0f

        private val adapter = DocumentaryAdapter(
            onFavoriteClick,
            onDocumentClick,
            onShareClick,
            onTextClick,
            onDeleteClick
        )

        init {
            recyclerView.adapter = adapter

            // Scroll listener for bottom pagination
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)

                    val type = currentMediaType ?: return

                    if (dy > 0 && !isLoadingMore && pageLoading[type] != true) {
                        val layoutManager = rv.layoutManager as? GridLayoutManager ?: return
                        val totalItemCount = layoutManager.itemCount
                        val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                        val fullList = pageLists[type] ?: emptyList()
                        val currentVisibleCount = pageVisibleCounts[type] ?: 6

                        if (lastVisibleItem >= totalItemCount - 1 && currentVisibleCount < fullList.size) {
                            loadNextBatch(type)
                        }
                    }
                }
            })

            // Upward swipe touch detection for when items fit screen without scrolling
            recyclerView.setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = event.rawY
                    }
                    MotionEvent.ACTION_UP -> {
                        view.performClick()
                        val endY = event.rawY
                        val difference = startY - endY

                        if (difference > 50) {
                            val type = currentMediaType ?: return@setOnTouchListener false
                            val fullList = pageLists[type] ?: emptyList()
                            val currentVisibleCount = pageVisibleCounts[type] ?: 6

                            if (currentVisibleCount < fullList.size && !view.canScrollVertically(1) && !isLoadingMore && pageLoading[type] != true) {
                                loadNextBatch(type)
                            }
                        }
                    }
                }
                false
            }
        }

        fun bind(mediaType: String) {
            currentMediaType = mediaType
            updateContent()
        }

        fun resetToFirst() {
            val type = currentMediaType ?: return
            pageVisibleCounts[type] = 6
            isLoadingMore = false
            bottomProgressBar.visibility = View.GONE
            recyclerView.post {
                try {
                    recyclerView.scrollToPosition(0)
                    updateSubmittedList()
                } catch (_: Exception) {}
            }
        }

        private fun loadNextBatch(type: String) {
            if (isLoadingMore) return
            val fullList = pageLists[type] ?: emptyList()
            val currentVisibleCount = pageVisibleCounts[type] ?: 6

            if (currentVisibleCount >= fullList.size) return

            isLoadingMore = true
            bottomProgressBar.visibility = View.VISIBLE

            recyclerView.postDelayed({
                val newCount = minOf(currentVisibleCount + 6, fullList.size)
                pageVisibleCounts[type] = newCount
                updateSubmittedList()
                bottomProgressBar.visibility = View.GONE
                isLoadingMore = false
            }, 500)
        }

        fun updateContent() {
            val type = currentMediaType ?: return

            // Center progress bar sirf tab dikhega jab pehli baar room database se list aana baaki ho
            if (pageLoading[type] == true && (pageLists[type]?.isEmpty() == true)) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }

            bottomProgressBar.visibility = View.GONE
            updateSubmittedList()
        }

        private fun updateSubmittedList() {
            val type = currentMediaType ?: return
            val fullList = pageLists[type] ?: emptyList()
            val count = pageVisibleCounts[type] ?: 6
            adapter.submitList(fullList.take(count))
        }
    }
}