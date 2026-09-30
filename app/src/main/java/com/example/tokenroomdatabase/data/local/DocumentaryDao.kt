package com.example.tokenroomdatabase.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tokenroomdatabase.data.model.Documentary
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentaryDao {

    @Insert
    suspend fun insertDocumentary(
        documentary: Documentary
    )

    @Update
    suspend fun updateDocumentary(
        documentary: Documentary
    )


    // UPDATE USER NAME AND IMAGE
    @Query("""
        UPDATE documentaries
        SET username = :username,
            userImageUri = :imageUri
        WHERE userEmail = :email
    """)
    suspend fun updateDocumentaryUserInfo(
        email: String,
        username: String,
        imageUri: String
    )


    // GET SINGLE DOCUMENTARY BY ID
    @Query("""
        SELECT * FROM documentaries
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getDocumentaryById(
        id: Int
    ): Documentary?


    // GET DOCUMENTARIES BY TYPE
    @Query("""
        SELECT * FROM documentaries
        WHERE (
            mediaType = :mediaType
            OR (:mediaType = 'image' AND mediaType = 'text')
        )
        ORDER BY id DESC
    """)
    fun getDocumentariesByType(
        mediaType: String
    ): Flow<List<Documentary>>

    // SEARCH DOCUMENTARIES
    @Query("""
        SELECT * FROM documentaries
        WHERE (
            mediaType = :mediaType
            OR (:mediaType = 'image' AND mediaType = 'text')
        )
        AND title LIKE '%' || :query || '%'
        ORDER BY id DESC
    """)
    fun searchDocumentariesByType(
        query: String,
        mediaType: String
    ): Flow<List<Documentary>>

    // GET FAVORITE DOCUMENTARIES
    @Query("""
        SELECT * FROM documentaries
        WHERE isFavorite = 1
        ORDER BY favoritedAt DESC, id DESC
    """)
    fun getFavoriteDocumentaries(): Flow<List<Documentary>>


    // NEW: DELETE DOCUMENTARY FROM ROOM
    @Query("""
        DELETE FROM documentaries
        WHERE id = :documentaryId
    """)
    suspend fun deleteDocumentaryById(
        documentaryId: Int
    )


    // NEW: REMOVE ONLY FROM FAVORITES
    @Query("""
        UPDATE documentaries
        SET isFavorite = 0
        WHERE id = :documentaryId
    """)
    suspend fun removeFromFavorite(
        documentaryId: Int
    )
}