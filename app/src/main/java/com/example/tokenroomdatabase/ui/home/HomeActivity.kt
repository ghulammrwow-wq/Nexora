package com.example.tokenroomdatabase.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.tokenroomdatabase.R
import com.example.tokenroomdatabase.data.local.UserDatabase
import com.example.tokenroomdatabase.data.model.Documentary
import com.example.tokenroomdatabase.databinding.ActivityHomeBinding
import com.example.tokenroomdatabase.ui.login.LoginActivity
import com.example.tokenroomdatabase.utils.TokenManager
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var database: UserDatabase
    private lateinit var tokenManager: TokenManager
    private lateinit var mediaPagerAdapter: MediaPagerAdapter

    private var documentaryJob: Job? = null

    private var userEmail = ""
    private var currentMediaType = "image"

    // ==================================================
    // APP FOREGROUND / BACKGROUND
    // ==================================================

    private val appLifecycleObserver =
        object : DefaultLifecycleObserver {

            override fun onStart(
                owner: LifecycleOwner
            ) {

                if (
                    !tokenManager.isSessionValid()
                ) {

                    Toast.makeText(
                        this@HomeActivity,
                        "Your session expired",
                        Toast.LENGTH_SHORT
                    ).show()

                    goToLogin()

                    return
                }

                tokenManager.updateLastActiveTime()
            }

            override fun onStop(
                owner: LifecycleOwner
            ) {

                tokenManager.updateLastActiveTime()
            }
        }

    // ==================================================
    // ON CREATE
    // ==================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        window.statusBarColor =
            getColor(
                R.color.white
            )

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding =
            ActivityHomeBinding.inflate(
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

        ProcessLifecycleOwner
            .get()
            .lifecycle
            .addObserver(
                appLifecycleObserver
            )

        if (
            !tokenManager.isSessionValid()
        ) {

            goToLogin()

            return
        }

        userEmail =
            intent.getStringExtra(
                "user_email"
            )
                ?: tokenManager.getUserEmail()
                        ?: ""

        if (
            userEmail.isEmpty()
        ) {

            goToLogin()

            return
        }

        setupBottomNavigation()

        setupDrawerButtons()

        setupDrawerListener()

        setupBackButton()

        loadDrawerUser()

        setupMediaPager()

        setupTabs()

        setupSearch()

        setupFab()
    }

    // ==================================================
    // MEDIA PAGER
    // ==================================================

    private fun setupMediaPager() {

        mediaPagerAdapter =
            MediaPagerAdapter(

                // ==================================================
                // FAVORITE
                // ==================================================

                onFavoriteClick = {
                        documentary ->

                    lifecycleScope.launch {

                        database.documentaryDao()
                            .updateDocumentary(

                                documentary.copy(
                                    isFavorite =
                                        !documentary.isFavorite
                                )
                            )
                    }
                },

                // ==================================================
                // DOCUMENT CLICK
                // ==================================================

                onDocumentClick = {
                        documentary ->

                    openDocumentary(
                        documentary.documentUri
                    )
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
                // TEXT CLICK
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
                // NEW: DELETE DOCUMENTARY
                // ==================================================

                onDeleteClick = {
                        documentary ->

                    showDeleteDocumentaryDialog(
                        documentary
                    )
                }
            )

        binding.mediaViewPager.adapter =
            mediaPagerAdapter

        TabLayoutMediator(
            binding.tabLayout,
            binding.mediaViewPager
        ) { tab, position ->

            tab.text =
                when (position) {

                    0 -> "Image"

                    1 -> "Video"

                    2 -> "PDF"

                    else -> ""
                }

        }.attach()

        binding.mediaViewPager
            .registerOnPageChangeCallback(

                object :
                    ViewPager2.OnPageChangeCallback() {

                    override fun onPageSelected(
                        position: Int
                    ) {

                        currentMediaType =
                            when (position) {

                                0 -> "image"

                                1 -> "video"

                                2 -> "pdf"

                                else -> "image"
                            }

                        val query =
                            binding.etSearchDocumentary
                                .text
                                .toString()
                                .trim()

                        loadDocumentaries(
                            query
                        )
                    }
                }
            )

        binding.mediaViewPager.setCurrentItem(
            0,
            false
        )

        loadDocumentaries(
            ""
        )
    }

    // ==================================================
    // LOAD DOCUMENTARIES
    // ==================================================

    private fun loadDocumentaries(
        query: String
    ) {

        documentaryJob?.cancel()

        documentaryJob =
            lifecycleScope.launch {

                val documentaryFlow =

                    if (
                        query.isEmpty()
                    ) {

                        database.documentaryDao()
                            .getDocumentariesByType(
                                currentMediaType
                            )

                    } else {

                        database.documentaryDao()
                            .searchDocumentariesByType(
                                query,
                                currentMediaType
                            )
                    }

                documentaryFlow.collect {
                        list ->

                    mediaPagerAdapter.updatePage(
                        currentMediaType,
                        list
                    )
                }
            }
    }

    // ==================================================
    // NEW: DELETE DOCUMENTARY CONFIRMATION
    // ==================================================

    private fun showDeleteDocumentaryDialog(
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
                        this@HomeActivity,
                        "Post deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    // ==================================================
    // TABS
    // ==================================================

    private fun setupTabs() {

        binding.tabLayout
            .addOnTabSelectedListener(

                object :
                    com.google.android.material.tabs.TabLayout.OnTabSelectedListener {

                    override fun onTabSelected(
                        tab: com.google.android.material.tabs.TabLayout.Tab
                    ) {
                    }

                    override fun onTabUnselected(
                        tab: com.google.android.material.tabs.TabLayout.Tab
                    ) {
                    }

                    override fun onTabReselected(
                        tab: com.google.android.material.tabs.TabLayout.Tab
                    ) {
                    }
                }
            )
    }

    // ==================================================
    // SEARCH
    // ==================================================

    private fun setupSearch() {

        binding.etSearchDocumentary
            .doAfterTextChanged {
                    text ->

                val query =
                    text.toString()
                        .trim()

                loadDocumentaries(
                    query
                )
            }
    }

    // ==================================================
    // FAB
    // ==================================================

    private fun setupFab() {

        binding.fabAddDocumentary
            .setOnClickListener {

                val options =
                    arrayOf(
                        "Post Image",
                        "Post Video",
                        "Post PDF",
                        "Post Text"
                    )

                AlertDialog.Builder(
                    this
                )
                    .setTitle(
                        "Create Post"
                    )
                    .setItems(
                        options
                    ) { _, which ->

                        when (which) {

                            0 -> {

                                openUploadActivity(
                                    "image"
                                )
                            }

                            1 -> {

                                openUploadActivity(
                                    "video"
                                )
                            }

                            2 -> {

                                openUploadActivity(
                                    "pdf"
                                )
                            }

                            3 -> {

                                val intent =
                                    Intent(
                                        this,
                                        PostTextActivity::class.java
                                    )

                                intent.putExtra(
                                    "user_email",
                                    userEmail
                                )

                                startActivity(
                                    intent
                                )
                            }
                        }
                    }
                    .show()
            }
    }

    // ==================================================
    // OPEN UPLOAD
    // ==================================================

    private fun openUploadActivity(
        postType: String
    ) {

        val intent =
            Intent(
                this,
                UploadDocumentaryActivity::class.java
            )

        intent.putExtra(
            "user_email",
            userEmail
        )

        intent.putExtra(
            "post_type",
            postType
        )

        startActivity(
            intent
        )
    }

    // ==================================================
    // OPEN DOCUMENT
    // ==================================================

    private fun openDocumentary(
        documentUri: String
    ) {

        if (
            documentUri.isEmpty()
        ) {

            Toast.makeText(
                this,
                "No file available",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        try {

            val uri =
                Uri.parse(
                    documentUri
                )

            val intent =
                Intent(
                    Intent.ACTION_VIEW
                )

            intent.setDataAndType(
                uri,
                contentResolver.getType(uri)
                    ?: "*/*"
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

    // ==================================================
    // SHARE
    // ==================================================

    private fun shareDocumentary(
        documentary: Documentary
    ) {

        try {

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

    // ==================================================
    // BOTTOM NAVIGATION
    // ==================================================

    private fun setupBottomNavigation() {

        binding.bottomNavigation
            .setOnItemSelectedListener {
                    item ->

                when (
                    item.itemId
                ) {

                    R.id.nav_home -> {

                        true
                    }

                    R.id.nav_favorite -> {

                        startActivity(
                            Intent(
                                this,
                                FavoriteActivity::class.java
                            )
                        )

                        true
                    }

                    R.id.nav_profile -> {

                        loadDrawerUser()

                        binding.drawerLayout
                            .openDrawer(
                                GravityCompat.START
                            )

                        true
                    }

                    else -> false
                }
            }

        binding.bottomNavigation
            .selectedItemId =
            R.id.nav_home
    }

    // ==================================================
    // DRAWER BUTTONS
    // ==================================================

    private fun setupDrawerButtons() {

        val drawer =
            binding.navigationView

        val imageView =
            drawer.findViewById<ImageView>(
                R.id.ivDrawerProfile
            )

        val username =
            drawer.findViewById<TextView>(
                R.id.tvDrawerUsername
            )

        val name =
            drawer.findViewById<TextView>(
                R.id.tvDrawerName
            )

        val email =
            drawer.findViewById<TextView>(
                R.id.tvDrawerEmail
            )

        val gender =
            drawer.findViewById<TextView>(
                R.id.tvDrawerGender
            )

        val deleteButton =
            drawer.findViewById<Button>(
                R.id.btnDrawerDelete
            )

        val logoutButton =
            drawer.findViewById<Button>(
                R.id.btnDrawerLogout
            )

        imageView.setOnClickListener {

            val intent =
                Intent(
                    this,
                    EditImageActivity::class.java
                )

            intent.putExtra(
                "user_email",
                userEmail
            )

            startActivity(
                intent
            )
        }

        username.setOnClickListener {

            val intent =
                Intent(
                    this,
                    EditUsernameActivity::class.java
                )

            intent.putExtra(
                "user_email",
                userEmail
            )

            startActivity(
                intent
            )
        }

        name.setOnClickListener {

            val intent =
                Intent(
                    this,
                    EditNameActivity::class.java
                )

            intent.putExtra(
                "user_email",
                userEmail
            )

            startActivity(
                intent
            )
        }

        email.setOnClickListener {

            val intent =
                Intent(
                    this,
                    EditEmailActivity::class.java
                )

            intent.putExtra(
                "user_email",
                userEmail
            )

            startActivity(
                intent
            )
        }

        gender.setOnClickListener {

            val intent =
                Intent(
                    this,
                    EditGenderActivity::class.java
                )

            intent.putExtra(
                "user_email",
                userEmail
            )

            startActivity(
                intent
            )
        }

        deleteButton.setOnClickListener {

            deleteUser()
        }

        logoutButton.setOnClickListener {

            logout()
        }
    }

    // ==================================================
    // DRAWER LISTENER
    // ==================================================

    private fun setupDrawerListener() {

        binding.drawerLayout.addDrawerListener(

            object :
                DrawerLayout.SimpleDrawerListener() {

                override fun onDrawerClosed(
                    drawerView: View
                ) {

                    super.onDrawerClosed(
                        drawerView
                    )

                    binding.bottomNavigation
                        .selectedItemId =
                        R.id.nav_home
                }
            }
        )
    }

    // ==================================================
    // BACK BUTTON
    // ==================================================

    private fun setupBackButton() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (
                        binding.drawerLayout.isDrawerOpen(
                            GravityCompat.START
                        )
                    ) {

                        binding.drawerLayout.closeDrawer(
                            GravityCompat.START
                        )

                        binding.bottomNavigation
                            .selectedItemId =
                            R.id.nav_home

                    } else {

                        isEnabled = false

                        onBackPressedDispatcher
                            .onBackPressed()
                    }
                }
            }
        )
    }

    // ==================================================
    // LOAD DRAWER USER
    // ==================================================

    private fun loadDrawerUser() {

        lifecycleScope.launch {

            val user =
                database.userDao()
                    .getUserByEmail(
                        userEmail
                    )

            if (
                user == null
            ) {

                goToLogin()

                return@launch
            }

            val drawer =
                binding.navigationView

            val imageView =
                drawer.findViewById<ImageView>(
                    R.id.ivDrawerProfile
                )

            val username =
                drawer.findViewById<TextView>(
                    R.id.tvDrawerUsername
                )

            val name =
                drawer.findViewById<TextView>(
                    R.id.tvDrawerName
                )

            val email =
                drawer.findViewById<TextView>(
                    R.id.tvDrawerEmail
                )

            val gender =
                drawer.findViewById<TextView>(
                    R.id.tvDrawerGender
                )

            val genderIcon =
                drawer.findViewById<ImageView>(
                    R.id.ivGender
                )

            username.text =
                user.username

            name.text =
                user.name

            email.text =
                user.email

            gender.text =
                user.gender

            if (
                user.gender.equals(
                    "Female",
                    ignoreCase = true
                )
            ) {

                genderIcon.setImageResource(
                    R.drawable.female
                )

            } else {

                genderIcon.setImageResource(
                    R.drawable.person_icon
                )
            }

            userEmail =
                user.email

            try {

                if (
                    user.imageUri.isNotEmpty()
                ) {

                    imageView.setImageURI(
                        Uri.parse(
                            user.imageUri
                        )
                    )

                } else {

                    imageView.setImageResource(
                        R.drawable.person_icon
                    )
                }

            } catch (_: Exception) {

                imageView.setImageResource(
                    R.drawable.person_icon
                )
            }
        }
    }

    // ==================================================
    // RESUME
    // ==================================================

    override fun onResume() {

        super.onResume()

        if (
            ::database.isInitialized
        ) {

            binding.bottomNavigation
                .selectedItemId =
                R.id.nav_home

            loadDrawerUser()
        }
    }

    // ==================================================
    // DELETE USER
    // ==================================================

    private fun deleteUser() {

        AlertDialog.Builder(
            this
        )
            .setTitle(
                "Delete Account"
            )
            .setMessage(
                "Are you sure you want to delete your account?"
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                lifecycleScope.launch {

                    val user =
                        database.userDao()
                            .getUserByEmail(
                                userEmail
                            )

                    if (
                        user != null
                    ) {

                        database.userDao()
                            .deleteUser(
                                user
                            )
                    }

                    tokenManager.clearToken()

                    Toast.makeText(
                        this@HomeActivity,
                        "Account deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    goToLogin()
                }
            }
            .show()
    }

    // ==================================================
    // LOGOUT
    // ==================================================

    private fun logout() {

        documentaryJob?.cancel()

        tokenManager.clearToken()

        Toast.makeText(
            this,
            "Your account is logout",
            Toast.LENGTH_SHORT
        ).show()

        goToLogin()
    }

    // ==================================================
    // GO TO LOGIN
    // ==================================================

    private fun goToLogin() {

        documentaryJob?.cancel()

        startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )

        finish()
    }

    // ==================================================
    // DESTROY
    // ==================================================

    override fun onDestroy() {

        ProcessLifecycleOwner
            .get()
            .lifecycle
            .removeObserver(
                appLifecycleObserver
            )

        documentaryJob?.cancel()

        super.onDestroy()
    }
}