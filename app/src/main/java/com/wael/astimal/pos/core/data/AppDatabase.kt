package com.wael.astimal.pos.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wael.astimal.pos.features.management.data.entity.EmployeeAccountTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.SaleCommissionEntity
import com.wael.astimal.pos.features.management.data.local.EmployeeFinancesDao
import com.wael.astimal.pos.features.inventory.data.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreProductStockEntity
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.local.dao.CategoryDao
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StockTransferDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreProductStockDao
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao
import com.wael.astimal.pos.features.management.data.entity.ClientEntity
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherEntity
import com.wael.astimal.pos.features.management.data.entity.SupplierEntity
import com.wael.astimal.pos.features.management.data.local.ClientDao
import com.wael.astimal.pos.features.management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.management.data.local.PurchaseDao
import com.wael.astimal.pos.features.management.data.local.PurchaseReturnDao
import com.wael.astimal.pos.features.management.data.local.ReceivePayVoucherDao
import com.wael.astimal.pos.features.management.data.local.SalesOrderDao
import com.wael.astimal.pos.features.management.data.local.SupplierDao
import com.wael.astimal.pos.features.user.data.entity.EmployeeStoreEntity
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.EmployeeDao
import com.wael.astimal.pos.features.user.data.local.UserDao

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
        ClientEntity::class,
        SupplierEntity::class,
        OrderEntity::class,
        OrderProductEntity::class,
        OrderReturnEntity::class,
        OrderReturnProductEntity::class,
        PurchaseEntity::class,
        PurchaseProductEntity::class,
        PurchaseReturnEntity::class,
        PurchaseReturnProductEntity::class,
        StoreProductStockEntity::class,
        SaleCommissionEntity::class,
        EmployeeAccountTransactionEntity::class,
        StockAdjustmentEntity::class,
        ReceivePayVoucherEntity::class
    ],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun unitDao(): UnitDao
    abstract fun storeDao(): StoreDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun stockTransferDao(): StockTransferDao
    abstract fun clientDao(): ClientDao
    abstract fun salesOrderDao(): SalesOrderDao
    abstract fun supplierDao(): SupplierDao
    abstract fun orderReturnDao(): OrderReturnDao
    abstract fun purchaseOrderDao(): PurchaseDao
    abstract fun purchaseReturnDao(): PurchaseReturnDao
    abstract fun storeProductStockDao(): StoreProductStockDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun employeeFinancesDao(): EmployeeFinancesDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao
    abstract fun receivePayVoucherDao(): ReceivePayVoucherDao
}