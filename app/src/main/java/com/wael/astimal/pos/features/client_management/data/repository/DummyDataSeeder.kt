package com.wael.astimal.pos.features.client_management.data.repository

import com.wael.astimal.pos.features.client_management.data.entity.ClientEntity
import com.wael.astimal.pos.features.client_management.data.local.ClientDao
import com.wael.astimal.pos.features.inventory.data.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.local.dao.CategoryDao
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StockTransferDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreDao
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DummyDataSeeder(
    private val userDao: UserDao,
    private val clientDao: ClientDao,
    private val storeDao: StoreDao,
    private val unitDao: UnitDao,
    private val categoryDao: CategoryDao,
    private val productDao: ProductDao,
    private val stockTransferDao: StockTransferDao,
    private val applicationScope: CoroutineScope
) {

    fun seedInitialDataIfNeeded() {
        // Launch in a coroutine to perform database operations off the main thread
        applicationScope.launch(Dispatchers.IO) {
            // Check if data already exists to avoid re-populating
            val userCount = userDao.getUserCount()
            if (userCount == 0) {
                populateAllDummyData()
            } else {
                println("Data already exists. Skipping dummy data seeding.")
            }
        }
    }

    private suspend fun populateAllDummyData() {
        println("Populating all dummy data...")

        // The order of population is important due to foreign key constraints.
        val employees = populateDummyUsersAndEmployees()
        val units = populateDummyUnits()
        val stores = populateDummyStores()
        val categories = populateDummyCategories()
        populateDummyClients(employees) // Now returns void
        val products = populateDummyProducts(categories, units, stores)
        populateDummyStockTransfers(stores, employees, products, units)

        println("Dummy data population complete.")
    }

    private suspend fun populateDummyUsersAndEmployees(): Map<String, Long> {
        val adminUser = UserEntity(
            id = 1,
            name = "Super Admin",
            arName = "مدير النظام",
            enName = "Super Admin",
            email = "super_admin@example.com",
            phone = "5551234567",
            isAdminFlag = true,
            isSynced = true
        )
        val adminLocalId = userDao.insertOrUpdate(adminUser)

        val emp1 = UserEntity(
            id = 2,
            name = "Default Employee One",
            arName = "موظف افتراضي ١",
            enName = "Default Employee One",
            email = "employee1@example.com",
            phone = "555000111",
            isEmployeeFlag = true,
            isSynced = true
        )
        val emp1LocalId = userDao.insertOrUpdate(emp1)

        val emp2 = UserEntity(
            id = 3,
            name = "Employee Two",
            arName = "موظف ٢",
            enName = "Employee Two",
            email = "employee2@example.com",
            phone = "555000222",
            isEmployeeFlag = true,
            isSynced = true
        )
        val emp2LocalId = userDao.insertOrUpdate(emp2)

        return mapOf("admin" to adminLocalId, "emp1" to emp1LocalId, "emp2" to emp2LocalId)
    }

    private suspend fun populateDummyClients(employees: Map<String, Long>) {
        val userForClient1 = UserEntity(
            id = -101,
            name = "Ahmed Mohamed",
            arName = "أحمد محمد",
            enName = "Ahmed Mohamed",
            email = "ahmed.client@example.com",
            phone = "1112223330",
            isClientFlag = true,
            isSynced = true
        )
        userDao.insertOrUpdate(userForClient1)

        val userForClient2 = UserEntity(
            id = -102,
            name = "Fatima Ali",
            arName = "فاطمة علي",
            enName = "Fatima Ali",
            email = "fatima.client@example.com",
            phone = "7778889990",
            isClientFlag = true,
            isSynced = true
        )
        userDao.insertOrUpdate(userForClient2)

        val client1Entity = ClientEntity(
            id = -201,
            arName = "عميل ١",
            enName = "Client 1",
            responsibleEmployeeLocalId = employees["emp1"],
            address = "123 Nile St, Cairo",
            debt = 250.50,
            isSupplier = false,
            isSynced = true,
            phone1 = "1112223330",
            phone2 = "1112223331",
            phone3 = null
        )
        clientDao.insertOrUpdateClient(client1Entity)

        val client2Entity = ClientEntity(
            id = -202,
            arName = "عميل ٢",
            enName = "Client 2",
            responsibleEmployeeLocalId = employees["emp2"],
            address = "456 Cornish Rd, Alexandria",
            debt = 0.0,
            isSupplier = true,
            isSynced = true,
            phone1 = "7778889990",
            phone2 = null,
            phone3 = null
        )
        clientDao.insertOrUpdateClient(client2Entity)
    }

    private suspend fun populateDummyUnits(): Map<String, Long> {
        val pieceId = unitDao.insertOrUpdate(
            UnitEntity(
                serverId = -1, arName = "قطعة", enName = "Piece", rate = 1.0f, isSynced = true
            )
        )
        val dozenId = unitDao.insertOrUpdate(
            UnitEntity(
                serverId = -2, arName = "دستة", enName = "Dozen", rate = 12.0f, isSynced = true
            )
        )
        val boxId = unitDao.insertOrUpdate(
            UnitEntity(
                serverId = -3, arName = "علبة", enName = "Box", rate = 24.0f, isSynced = true
            )
        )
        return mapOf("piece" to pieceId, "dozen" to dozenId, "box" to boxId)
    }

    private suspend fun populateDummyStores(): Map<String, Long> {
        val mainStoreId = storeDao.insertOrUpdate(
            StoreEntity(
                serverId = -1,
                arName = "المخزن الرئيسي",
                enName = "Main Warehouse",
                type = StoreType.MAIN,
                isSynced = true
            )
        )
        val storeAId = storeDao.insertOrUpdate(
            StoreEntity(
                serverId = -2,
                arName = "فرع أ",
                enName = "Branch A",
                type = StoreType.SUB,
                isSynced = true
            )
        )
        val storeBId = storeDao.insertOrUpdate(
            StoreEntity(
                serverId = -3,
                arName = "فرع ب",
                enName = "Branch B",
                type = StoreType.SUB,
                isSynced = true
            )
        )
        return mapOf("main" to mainStoreId, "storeA" to storeAId, "storeB" to storeBId)
    }

    private suspend fun populateDummyCategories(): Map<String, Long> {
        val lensesId = categoryDao.insertOrUpdate(
            CategoryEntity(
                serverId = -1, arName = "عدسات", enName = "Lenses", isSynced = true
            )
        )
        val solutionsId = categoryDao.insertOrUpdate(
            CategoryEntity(
                serverId = -2, arName = "محاليل", enName = "Solutions", isSynced = true
            )
        )
        val accessoriesId = categoryDao.insertOrUpdate(
            CategoryEntity(
                serverId = -3, arName = "اكسسوارات", enName = "Accessories", isSynced = true
            )
        )
        return mapOf(
            "lenses" to lensesId, "solutions" to solutionsId, "accessories" to accessoriesId
        )
    }

    private suspend fun populateDummyProducts(
        categories: Map<String, Long>, units: Map<String, Long>, stores: Map<String, Long>
    ): Map<String, Long> {
        val product1 = ProductEntity(
            serverId = -1,
            arName = "عدسات ديزيو الشهرية",
            enName = "Desio Monthly Lenses",
            categoryId = categories["lenses"],
            averagePrice = 120.0,
            sellingPrice = 180.0,
            openingBalanceQuantity = 50.0,
            storeId = stores["main"],
            isSynced = true,
            minimumStockUnitId = units["box"],
            maximumStockUnitId = units["box"],
            minimumStockLevel = 10,
            maximumStockLevel = 100,
            firstPeriodData = null
        )
        val prod1Id = productDao.insertOrUpdate(product1)

        val product2 = ProductEntity(
            serverId = -2,
            arName = "محلول أوبتي-فري",
            enName = "Opti-Free Solution",
            categoryId = categories["solutions"],
            averagePrice = 35.5,
            sellingPrice = 55.0,
            openingBalanceQuantity = 100.0,
            storeId = stores["main"],
            isSynced = true,
            minimumStockUnitId = units["piece"],
            maximumStockUnitId = units["piece"],
            minimumStockLevel = 20,
            maximumStockLevel = 200,
            firstPeriodData = "Initial Stock"
        )
        val prod2Id = productDao.insertOrUpdate(product2)

        val product3 = ProductEntity(
            serverId = -3,
            arName = "حافظة عدسات",
            enName = "Lens Case",
            categoryId = categories["accessories"],
            averagePrice = 5.0,
            sellingPrice = 15.0,
            openingBalanceQuantity = 200.0,
            storeId = stores["main"],
            isSynced = true,
            minimumStockUnitId = units["piece"],
            maximumStockUnitId = units["dozen"],
            minimumStockLevel = 50,
            maximumStockLevel = 500,
            firstPeriodData = null
        )
        val prod3Id = productDao.insertOrUpdate(product3)
        return mapOf("desio" to prod1Id, "optiFree" to prod2Id, "lensCase" to prod3Id)
    }

    private suspend fun populateDummyStockTransfers(
        stores: Map<String, Long>,
        employees: Map<String, Long>,
        products: Map<String, Long>,
        units: Map<String, Long>
    ) {
        val transfer1 = StockTransferEntity(
            serverId = -1,
            fromStoreId = stores["main"],
            toStoreId = stores["storeA"],
            initiatedByUserId = employees["admin"],
            transferDate = System.currentTimeMillis() - 86400000 // 1 day ago
        )
        val transfer1LocalId = stockTransferDao.insertStockTransfer(transfer1)
        stockTransferDao.insertStockTransferItems(
            listOf(
                StockTransferItemEntity(
                    stockTransferLocalId = transfer1LocalId,
                    productLocalId = products["desio"]!!,
                    unitLocalId = units["box"]!!,
                    quantity = 10.0,
                    maximumOpeningBalance = null,
                    minimumOpeningBalance = null,
                    localId = -1,
                    serverId = null
                ), StockTransferItemEntity(
                    stockTransferLocalId = transfer1LocalId,
                    productLocalId = products["optiFree"]!!,
                    unitLocalId = units["piece"]!!,
                    quantity = 20.0,
                    maximumOpeningBalance = null,
                    minimumOpeningBalance = null,
                    localId = -2,
                    serverId = null
                )
            )
        )

        val transfer2 = StockTransferEntity(
            serverId = -2,
            fromStoreId = stores["main"],
            toStoreId = stores["storeB"],
            initiatedByUserId = employees["emp1"],
            transferDate = System.currentTimeMillis()
        )
        val transfer2LocalId = stockTransferDao.insertStockTransfer(transfer2)
        stockTransferDao.insertStockTransferItems(
            listOf(
                StockTransferItemEntity(
                    stockTransferLocalId = transfer2LocalId,
                    productLocalId = products["lensCase"]!!,
                    unitLocalId = units["dozen"]!!,
                    quantity = 5.0,
                    maximumOpeningBalance = null,
                    minimumOpeningBalance = null,
                    localId = -3,
                    serverId = null
                )
            )
        )
    }
}