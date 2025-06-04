package com.wael.astimal.pos.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wael.astimal.pos.features.inventory.data.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.local.dao.CategoryDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreDao
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao

@Database(
    entities = [UnitEntity::class, StoreEntity::class, CategoryEntity::class],
    version = 3,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun unitDao(): UnitDao
    abstract fun storeDao(): StoreDao
    abstract fun categoryDao(): CategoryDao
}