package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.features.inventory.data.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.local.dao.CategoryDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreDao
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.data.entity.ClientEntity
import com.wael.astimal.pos.features.management.data.entity.SupplierEntity
import com.wael.astimal.pos.features.management.data.local.ClientDao
import com.wael.astimal.pos.features.management.data.local.SupplierDao
import com.wael.astimal.pos.features.user.data.entity.EmployeeStoreEntity
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.EmployeeDao
import com.wael.astimal.pos.features.user.data.local.UserDao
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DummyDataSeeder(
    private val userDao: UserDao,
    private val clientDao: ClientDao,
    private val supplierDao: SupplierDao,
    private val storeDao: StoreDao,
    private val unitDao: UnitDao,
    private val categoryDao: CategoryDao,
    private val employeeDao: EmployeeDao,
    private val sessionManager: SessionManager,
    private val productRep: ProductRepository,
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
        val stores = populateDummyStores()

        assignEmployeesToStores(employees, stores)
        sessionManager.saveSession(
            employees["emp1"]!!,
            ""
        )

        val units = populateDummyUnits()
        val categories = populateDummyCategories()
        populateDummyClients(employees)
        populateDummySuppliers(employees)
        populateDummyProducts(categories, units, stores)

        println("Dummy data population complete.")
    }

    private suspend fun assignEmployeesToStores(
        employees: Map<String, Long>, stores: Map<String, Long>
    ) {
        employeeDao.assignStoreToEmployee(
            EmployeeStoreEntity(
                employeeLocalId = employees["emp1"]!!, storeLocalId = stores["storeA"]!!
            )
        )
        employeeDao.assignStoreToEmployee(
            EmployeeStoreEntity(
                employeeLocalId = employees["emp2"]!!, storeLocalId = stores["storeB"]!!
            )
        )
    }

    private suspend fun populateDummySuppliers(employees: Map<String, Long>) {
        val supplier1Entity = SupplierEntity(
            id = -301,
            arName = "مورد ١",
            enName = "Supplier 1",
            responsibleEmployeeLocalId = employees["emp1"],
            address = "789 Supply St, Cairo",
            isSynced = false,
            phone = "3334445550",
            indebtedness = 0.0,
            isClient = true,
            isDeletedLocally = false,
        )
        supplierDao.insertOrUpdateSupplier(supplier1Entity)
        val supplier2Entity = SupplierEntity(
            id = -302,
            arName = "مورد ٢",
            enName = "Supplier 2",
            responsibleEmployeeLocalId = employees["emp2"],
            address = "101 Supply Rd, Alexandria",
            isSynced = false,
            phone = "6667778880",
            indebtedness = 150.75,
            isClient = false,
            isDeletedLocally = false,
        )
        supplierDao.insertOrUpdateSupplier(supplier2Entity)
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
            isSynced = false
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
            isSynced = false
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
            isSynced = false
        )
        val emp2LocalId = userDao.insertOrUpdate(emp2)

        return mapOf("admin" to adminLocalId, "emp1" to emp1LocalId, "emp2" to emp2LocalId)
    }

    private suspend fun populateDummyClients(employees: Map<String, Long>) {
        val client1Entity = ClientEntity(
            id = -201,
            arName = "عميل ١",
            enName = "Client 1",
            responsibleEmployeeLocalId = employees["emp1"],
            address = "123 Nile St, Cairo",
            debt = 250.50,
            isSupplier = false,
            isSynced = false,
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
            isSynced = false,
            phone1 = "7778889990",
            phone2 = null,
            phone3 = null
        )
        clientDao.insertOrUpdateClient(client2Entity)
    }

    private suspend fun populateDummyUnits(): Map<String, Long> {
        val pieceId = unitDao.insertOrUpdate(
            UnitEntity(
                serverId = -1, arName = "قطعة", enName = "Piece", isSynced = false
            )
        )
        val dozenId = unitDao.insertOrUpdate(
            UnitEntity(
                serverId = -2, arName = "دستة", enName = "Dozen", isSynced = false
            )
        )
        val boxId = unitDao.insertOrUpdate(
            UnitEntity(
                serverId = -3, arName = "علبة", enName = "Box", isSynced = false
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
                isSynced = false
            )
        )
        val storeAId = storeDao.insertOrUpdate(
            StoreEntity(
                serverId = -2,
                arName = "فرع أ",
                enName = "Branch A",
                type = StoreType.SUB,
                isSynced = false
            )
        )
        val storeBId = storeDao.insertOrUpdate(
            StoreEntity(
                serverId = -3,
                arName = "فرع ب",
                enName = "Branch B",
                type = StoreType.SUB,
                isSynced = false
            )
        )
        return mapOf("main" to mainStoreId, "storeA" to storeAId, "storeB" to storeBId)
    }

    private suspend fun populateDummyCategories(): Map<String, Long> {
        val lensesId = categoryDao.insertOrUpdate(
            CategoryEntity(
                serverId = -1, arName = "عدسات", enName = "Lenses", isSynced = false
            )
        )
        val solutionsId = categoryDao.insertOrUpdate(
            CategoryEntity(
                serverId = -2, arName = "محاليل", enName = "Solutions", isSynced = false
            )
        )
        val accessoriesId = categoryDao.insertOrUpdate(
            CategoryEntity(
                serverId = -3, arName = "اكسسوارات", enName = "Accessories", isSynced = false
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
            localId = 1,
            serverId = -1,
            arName = "عدسات ديزيو الشهرية",
            enName = "Desio Monthly Lenses",
            categoryId = categories["lenses"],
            averagePrice = 120.0,
            sellingPrice = 180.0,
            openingBalanceQuantity = 50.0,
            storeId = stores["main"],
            isSynced = false,
            minimumUnitId = null,
            maximumUnitId = units["box"],
            subUnitsPerMainUnit = 1.0,
        )
        productRep.addProduct(product1)

        val product2 = ProductEntity(
            localId = 2,
            serverId = -2,
            arName = "محلول أوبتي-فري",
            enName = "Opti-Free Solution",
            categoryId = categories["solutions"],
            averagePrice = 35.5,
            sellingPrice = 55.0,
            openingBalanceQuantity = 100.0,
            storeId = stores["main"],
            isSynced = false,
            minimumUnitId = null,
            maximumUnitId = units["piece"],
            subUnitsPerMainUnit = 1.0,
        )
        productRep.addProduct(product2)

        val product3 = ProductEntity(
            localId = 3,
            serverId = -3,
            arName = "حافظة عدسات",
            enName = "Lens Case",
            categoryId = categories["accessories"],
            averagePrice = 5.0,
            sellingPrice = 15.0,
            openingBalanceQuantity = 200.0,
            storeId = stores["main"],
            isSynced = false,
            minimumUnitId = units["piece"],
            maximumUnitId = units["dozen"],
            subUnitsPerMainUnit = 12.0,
        )
        productRep.addProduct(product3)
        return mapOf("desio" to 1, "optiFree" to 2, "lensCase" to 3)
    }
}