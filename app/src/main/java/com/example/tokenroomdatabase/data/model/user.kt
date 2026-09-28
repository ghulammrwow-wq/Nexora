package com.example.tokenroomdatabase.data.model



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val username: String,

    val name: String,

    val email: String,

    val password: String,

    val gender: String,

    val imageUri: String
)