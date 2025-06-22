package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.inventory.data.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.user.data.entity.EmployeeStoreEntity
import com.wael.astimal.pos.features.user.data.local.UserDao
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DummyDataSeeder(
    private val navigator: NavigationController,
    private val userDao: UserDao,
    private val userRepository: UserRepository,
    private val businessPartnerRepository: BusinessPartnerRepository,
    private val productRep: ProductRepository,
    private val storeRepository: StoreRepository,
    private val unitRepository: UnitRepository,
    private val categoryRepository: CategoryRepository,
    private val applicationScope: CoroutineScope,
) {
    fun seedInitialDataIfNeeded() {
        applicationScope.launch(Dispatchers.IO) {
            val userCount = userRepository.getEmployeesFlow().first().count()
            if (userCount == 0) {
                populateAllDummyData()
            } else {
                println("Data already exists. Skipping dummy data seeding.")
            }
        }
    }

    private suspend fun populateAllDummyData() {
        println("Populating all dummy data...")

        val user = userRepository.login(
            "s@mail.com", "46554655"
        ).onSuccess {
            navigator.navigate(
                Destination.Main,
                Destination.Auth,
                true
            )
        }.getOrElse {
            println("Error logging in: ${it.message}")
            return@populateAllDummyData
        }

        val stores = populateDummyStores()
        assignEmployeesToStores(stores, user)

        val units = populateDummyUnits()
        val categories = populateDummyCategories()
        populateDummyBusinessPartner(user)
        populateDummyProducts(stores, categories, units)

        println("Dummy data population complete.")
    }

    private suspend fun assignEmployeesToStores(stores: List<Store>, user: User) {
        stores.forEach {
            userDao.assignStoreToEmployee(
                EmployeeStoreEntity(
                    employeeLocalId = user.id, storeLocalId = it.id.local
                )
            )
        }
    }


    private suspend fun populateDummyBusinessPartner(user: User): List<BusinessPartner> {
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
                responsibleEmployee = user,
                supplierIndebtedness = 350.0,
                clientDebt = 500.0,
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
                responsibleEmployee = user,
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
                responsibleEmployee = user,
                clientDebt = 250.50,
                type = PartnerType.CLIENT,
                isSynced = false
            )
        )

        businessPartners.forEach {
            businessPartnerRepository.saveBusinessPartner(it)
        }

        return businessPartnerRepository.getBusinessPartners().first()
    }

    private suspend fun populateDummyUnits(): List<UnitEntity> {
        val list = listOf(
            UnitEntity(
                localId = -1, serverId = -1, arName = "قطعة", enName = "Piece", isSynced = false
            ), UnitEntity(
                localId = -2, serverId = -2, arName = "دستة", enName = "Dozen", isSynced = false
            ), UnitEntity(
                localId = -3, serverId = -3, arName = "علبة", enName = "Box", isSynced = false
            )
        )
        list.forEach {
            unitRepository.saveUnit(it)
        }
        return list
    }

    private suspend fun populateDummyStores(): List<Store> {
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

        stores.forEach {
            storeRepository.saveStore(it)
        }
        return storeRepository.getStores().first()
    }

    private suspend fun populateDummyCategories(): List<Category> {
        val list = listOf(
            CategoryEntity(
                localId = -1, serverId = null, arName = "عدسات", enName = "Lenses", isSynced = false
            ), CategoryEntity(
                localId = -2,
                serverId = null,
                arName = "محاليل",
                enName = "Solutions",
                isSynced = false
            ), CategoryEntity(
                localId = -3,
                serverId = null,
                arName = "اكسسوارات",
                enName = "Accessories",
                isSynced = false
            )
        )
        list.forEach { categoryRepository.saveCategory(it) }
        return categoryRepository.getCategories().first()
    }

    private suspend fun populateDummyProducts(
        stores: List<Store>, categories: List<Category>, units: List<UnitEntity>
    ): List<Product> {
        val list = listOf(
            ProductEntity(
                localId = 0,
                serverId = null,
                arName = "عدسات ديزيو الشهرية",
                enName = "Desio Monthly Lenses",
                categoryId = categories[0].id.local,
                averagePrice = 120.0,
                sellingPrice = 180.0,
                openingBalanceQuantity = 50.0,
                storeId = stores[1].id.local,
                minimumUnitId = null,
                maximumUnitId = units[2].localId,
                subUnitsPerMainUnit = 1.0
            ), ProductEntity(
                localId = 0,
                serverId = -2,
                arName = "محلول أوبتي-فري",
                enName = "Opti-Free Solution",
                categoryId = categories[1].id.local,
                averagePrice = 35.5,
                sellingPrice = 55.0,
                openingBalanceQuantity = 100.0,
                storeId = stores[1].id.local,
                isSynced = false,
                minimumUnitId = null,
                maximumUnitId = units[0].localId,
                subUnitsPerMainUnit = 1.0
            ), ProductEntity(
                localId = 0,
                serverId = -3,
                arName = "حافظة عدسات",
                enName = "Lens Case",
                categoryId = categories[2].id.local,
                averagePrice = 5.0,
                sellingPrice = 15.0,
                openingBalanceQuantity = 200.0,
                storeId = stores[1].id.local,
                isSynced = false,
                minimumUnitId = units[0].localId, // Piece
                maximumUnitId = units[1].localId, // Dozen
                subUnitsPerMainUnit = 12.0
            )
        )
        list.forEach {
            productRep.saveProduct(it)
        }
        return productRep.getProducts().first()
    }
}
