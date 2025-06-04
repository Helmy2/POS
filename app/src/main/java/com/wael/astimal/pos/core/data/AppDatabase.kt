package com.wael.astimal.pos.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao

@Database(
    entities = [UnitEntity::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun unitDao(): UnitDao
}