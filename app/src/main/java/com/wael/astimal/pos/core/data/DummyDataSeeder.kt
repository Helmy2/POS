package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.data.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.local.dao.CategoryDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreDao
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.user.data.entity.EmployeeStoreEntity
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain
import com.wael.astimal.pos.features.user.data.local.SessionManager
import com.wael.astimal.pos.features.user.data.local.UserDao
import com.wael.astimal.pos.features.user.domain.entity.UserType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DummyDataSeeder(
    private val userDao: UserDao,
    private val storeDao: StoreDao,
    private val unitDao: UnitDao,
    private val categoryDao: CategoryDao,
    private val sessionManager: SessionManager,
    private val productRep: ProductRepository,
    private val businessPartnerRepository: BusinessPartnerRepository,
    private val applicationScope: CoroutineScope,
) {

    val users = listOf(
        UserEntity(
            id = 1,
            name = "Super Admin",
            arName = "مدير النظام",
            enName = "Super Admin",
            email = "super_admin@example.com",
            phone = "5551234567",
            userType = UserType.ADMIN,
            avatarUrl = "",
        ), UserEntity(
            id = 2,
            name = "Default Employee One",
            arName = "موظف افتراضي ١",
            enName = "Default Employee One",
            email = "employee1@example.com",
            phone = "555000111",
            userType = UserType.EMPLOYEE,
            avatarUrl = "",
        ), UserEntity(
            id = 3,
            name = "Employee Two",
            arName = "موظف ٢",
            enName = "Employee Two",
            email = "employee2@example.com",
            phone = "555000222",
            userType = UserType.EMPLOYEE,
            avatarUrl = "",
        )
    )

    val stores = listOf(
        StoreEntity(
            localId = -1,
            serverId = -1,
            arName = "المخزن الرئيسي",
            enName = "Main Warehouse",
            type = StoreType.MAIN,
            isSynced = false
        ), StoreEntity(
            localId = -2,
            serverId = -2,
            arName = "فرع أ",
            enName = "Branch A",
            type = StoreType.SUB,
            isSynced = false
        ), StoreEntity(
            localId = -3,
            serverId = -3,
            arName = "فرع ب",
            enName = "Branch B",
            type = StoreType.SUB,
            isSynced = false
        )
    )

    fun seedInitialDataIfNeeded() {
        applicationScope.launch(Dispatchers.IO) {
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

        populateDummyUsersAndEmployees()
        populateDummyStores()
        assignEmployeesToStores()

        sessionManager.saveUserSession(users.elementAt(0).id, "", "", "")

        val units = populateDummyUnits()
        val categories = populateDummyCategories()
        populateDummyBusinessPartner()
        populateDummyProducts(categories, units)

        println("Dummy data population complete.")
    }

    private suspend fun assignEmployeesToStores() {
        userDao.assignStoreToEmployee(
            EmployeeStoreEntity(
                employeeLocalId = users.elementAt(1).id, storeLocalId = stores.elementAt(1).localId
            )
        )
        userDao.assignStoreToEmployee(
            EmployeeStoreEntity(
                employeeLocalId = users.elementAt(2).id, storeLocalId = stores.elementAt(2).localId
            )
        )
    }


    private suspend fun populateDummyBusinessPartner() {
        val businessPartners = listOf(
            BusinessPartner(
                clientLocalId = null,
                supplierLocalId = null,
                name = LocalizedString(
                    arName = "شريك عالمي",
                    enName = "Universal Partner",
                ),
                address = "1 El Tahrir Square, Cairo",
                phone = "0123456789",
                responsibleEmployee = users.elementAt(2).toDomain(),
                supplierIndebtedness = 350.0,
                type = PartnerType.BOTH,
                isSynced = false
            ), BusinessPartner(
                clientLocalId = null,
                supplierLocalId = null,
                name = LocalizedString(
                    arName = "مورد ٢ (فقط)",
                    enName = "Supplier Two (Only)",
                ),
                address = "1 El Tahrir Square, Cairo",
                phone = "6667778880",
                responsibleEmployee = users.elementAt(1).toDomain(),
                supplierIndebtedness = 150.0,
                type = PartnerType.SUPPLIER,
                isSynced = false
            ), BusinessPartner(
                clientLocalId = null,
                supplierLocalId = null,
                name = LocalizedString(
                    arName = "عميل ١ (فقط)",
                    enName = "Client One (Only)",
                ),
                address = "1 El Tahrir Square, Cairo",
                phone = "0123412352235",
                responsibleEmployee = users.elementAt(1).toDomain(),
                clientDebt = 250.50,
                type = PartnerType.CLIENT,
                isSynced = false
            )

        )

        businessPartners.forEach {
            businessPartnerRepository.saveBusinessPartner(it)
        }
    }

    private suspend fun populateDummyUsersAndEmployees() {
        users.forEach {
            userDao.insertOrUpdate(it)
        }
    }

    private suspend fun populateDummyUnits(): Map<String, Long> {
        val pieceId = unitDao.insertOrUpdate(
            UnitEntity(serverId = -1, arName = "قطعة", enName = "Piece", isSynced = false)
        )
        val dozenId = unitDao.insertOrUpdate(
            UnitEntity(serverId = -2, arName = "دستة", enName = "Dozen", isSynced = false)
        )
        val boxId = unitDao.insertOrUpdate(
            UnitEntity(serverId = -3, arName = "علبة", enName = "Box", isSynced = false)
        )
        return mapOf("piece" to pieceId, "dozen" to dozenId, "box" to boxId)
    }

    private suspend fun populateDummyStores() {
        stores.forEach {
            storeDao.insertOrUpdate(it)
        }
    }

    private suspend fun populateDummyCategories(): Map<String, Long> {
        val lensesId = categoryDao.insertOrUpdate(
            CategoryEntity(
                localId = -1,
                serverId = null,
                arName = "عدسات",
                enName = "Lenses",
                isSynced = false
            )
        )
        val solutionsId = categoryDao.insertOrUpdate(
            CategoryEntity(
                localId = -2,
                serverId = null, arName = "محاليل", enName = "Solutions", isSynced = false
            )
        )
        val accessoriesId = categoryDao.insertOrUpdate(
            CategoryEntity(
                localId = -3,
                serverId = null, arName = "اكسسوارات", enName = "Accessories", isSynced = false
            )
        )
        return mapOf(
            "lenses" to lensesId, "solutions" to solutionsId, "accessories" to accessoriesId
        )
    }

    private suspend fun populateDummyProducts(
        categories: Map<String, Long>, units: Map<String, Long>
    ): Map<String, Long> {
        val product1 = ProductEntity(
            localId = -1,
            serverId = -1,
            arName = "عدسات ديزيو الشهرية",
            enName = "Desio Monthly Lenses",
            categoryId = categories["lenses"]!!,
            averagePrice = 120.0,
            sellingPrice = 180.0,
            openingBalanceQuantity = 50.0,
            storeId = stores.elementAt(1).localId,
            isSynced = false,
            minimumUnitId = null,
            maximumUnitId = units["box"]!!,
            subUnitsPerMainUnit = 1.0,
        )
        productRep.addProduct(product1)

        val product2 = ProductEntity(
            localId = -2,
            serverId = -2,
            arName = "محلول أوبتي-فري",
            enName = "Opti-Free Solution",
            categoryId = categories["solutions"]!!,
            averagePrice = 35.5,
            sellingPrice = 55.0,
            openingBalanceQuantity = 100.0,
            storeId = stores.elementAt(1).localId,
            isSynced = false,
            minimumUnitId = null,
            maximumUnitId = units["piece"]!!,
            subUnitsPerMainUnit = 1.0,
        )
        productRep.addProduct(product2)

        val product3 = ProductEntity(
            localId = -3,
            serverId = -3,
            arName = "حافظة عدسات",
            enName = "Lens Case",
            categoryId = categories["accessories"]!!,
            averagePrice = 5.0,
            sellingPrice = 15.0,
            openingBalanceQuantity = 200.0,
            storeId = stores.elementAt(1).localId,
            isSynced = false,
            minimumUnitId = units["piece"],
            maximumUnitId = units["dozen"]!!,
            subUnitsPerMainUnit = 12.0,
        )
        productRep.addProduct(product3)
        return mapOf("desio" to 1, "optiFree" to 2, "lensCase" to 3)
    }
}
