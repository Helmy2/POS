package com.wael.astimal.pos.features.inventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.inventory.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(category: CategoryEntity): Long

    @Query("SELECT * FROM categories WHERE localId = :localId LIMIT 1")
    suspend fun getCategoryByLocalId(localId: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE NOT isDeletedLocally AND (arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%') ORDER BY enName ASC, arName ASC")
    fun searchCategoriesFlow(query: String): Flow<List<CategoryEntity>>
}