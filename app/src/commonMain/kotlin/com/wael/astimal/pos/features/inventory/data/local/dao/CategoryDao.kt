package com.wael.astimal.pos.features.inventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.inventory.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(category: CategoryEntity): Long

    @Query("SELECT * FROM categories WHERE localId = :localId LIMIT 1")
    suspend fun getCategoryById(localId: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE NOT isDeletedLocally AND (arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%') ORDER BY enName ASC, arName ASC")
    fun searchCategoriesFlow(query: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE localId = :localId")
    suspend fun deleteCategoryById(localId: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}