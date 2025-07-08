package com.wael.astimal.pos.core.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.wael.astimal.pos.features.inventory.data.local.dao.CategoryDao
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StockTransferDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreDao
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao
import com.wael.astimal.pos.features.inventory.data.local.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.UnitEntity
import com.wael.astimal.pos.features.management.data.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.SaleCommissionEntity
import com.wael.astimal.pos.features.management.data.local.BusinessPartnerDao
import com.wael.astimal.pos.features.management.data.local.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.PurchaseDao
import com.wael.astimal.pos.features.management.data.local.PurchaseReturnDao
import com.wael.astimal.pos.features.management.data.local.SalesOrderDao
import com.wael.astimal.pos.features.user.data.local.UserDao
import com.wael.astimal.pos.features.user.data.local.entity.EmployeeStoreEntity
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        EmployeeStoreEntity::class,
        UnitEntity::class,
        StoreEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        StockTransferEntity::class,
        StockTransferItemEntity::class,
        BusinessPartnerEntity::class,
        OrderEntity::class,
        OrderProductEntity::class,
        OrderReturnEntity::class,
        OrderReturnProductEntity::class,
        PurchaseEntity::class,
        PurchaseProductEntity::class,
        PurchaseReturnEntity::class,
        PurchaseReturnProductEntity::class,
        SaleCommissionEntity::class,
        EmployeeAccountTransactionEntity::class,
        StockAdjustmentEntity::class,
        PartnerTransactionEntity::class
    ],
    version = 11,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun unitDao(): UnitDao
    abstract fun storeDao(): StoreDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun stockTransferDao(): StockTransferDao
    abstract fun businessPartnerDao(): BusinessPartnerDao
    abstract fun salesOrderDao(): SalesOrderDao
    abstract fun orderReturnDao(): OrderReturnDao
    abstract fun purchaseOrderDao(): PurchaseDao
    abstract fun purchaseReturnDao(): PurchaseReturnDao
    abstract fun employeeFinancesDao(): EmployeeFinancesDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao
    abstract fun partnerTransactionDao(): PartnerTransactionDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}