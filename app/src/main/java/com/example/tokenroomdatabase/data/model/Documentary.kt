package com.example.tokenroomdatabase.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documentaries")
data class Documentary(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String,

    val documentUri: String,

    val mediaType: String,

    val isFavorite: Boolean = false,

    val userEmail: String,

    val username: String,

    val userImageUri: String,

    val favoritedAt: Long = 0L
)