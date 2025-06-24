package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
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
                clientId = null,
                supplierId = null,
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
                clientId = null,
                supplierId = null,
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
                clientId = null,
                supplierId = null,
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

    private suspend fun populateDummyUnits(): List<ProductUnit> {
        val list = listOf(
            ProductUnit(
                id = Id.new,
                name = LocalizedString(
                    arName = "قطعة",
                    enName = "Piece"
                ),
                createdAt = Clock.now(),
            ),
            ProductUnit(
                id = Id.new,
                name = LocalizedString(
                    arName = "دزينة",
                    enName = "Dozen"
                ),
                createdAt = Clock.now(),
            ), ProductUnit(
                id = Id.new,
                name = LocalizedString(
                    arName = "علبة",
                    enName = "Box"
                ),
                createdAt = Clock.now(),
            )
        )
        list.forEach {
            unitRepository.saveUnit(it)
        }
        return unitRepository.getUnits("").first()
    }

    private suspend fun populateDummyStores(): List<Store> {
        val stores = listOf(
            Store(
                id = Id.new,
                name = LocalizedString(
                    arName = "المخزن الرئيسي",
                    enName = "Main Warehouse"
                ),
                type = StoreType.MAIN,
                isSynced = false,
                createdAt = Clock.now(),
            ),
            Store(
                id = Id.new,
                name = LocalizedString(
                    arName = "فرع أ",
                    enName = "Branch A"
                ),
                type = StoreType.SUB,
                isSynced = false,
                createdAt = Clock.now(),
            ), Store(
                id = Id.new,
                name = LocalizedString(
                    arName = "فرع ب",
                    enName = "Branch B"
                ),
                type = StoreType.SUB,
                isSynced = false,
                createdAt = Clock.now(),
            )
        )

        stores.forEach {
            storeRepository.saveStore(it)
        }
        return storeRepository.getStores().first()
    }

    private suspend fun populateDummyCategories(): List<Category> {
        val list = listOf(
            Category(
                id = Id.new,
                name = LocalizedString(
                    arName = "عدسات",
                    enName = "Lenses"
                ),
                createdAt = Clock.now(),
            ), Category(
                id = Id.new,
                name = LocalizedString(
                    arName = "محاليل",
                    enName = "Solutions"
                ),
                createdAt = Clock.now(),
            ), Category(
                id = Id.new,
                name = LocalizedString(
                    arName = "اكسسوارات",
                    enName = "Accessories"
                ),
                createdAt = Clock.now(),
            )
        )
        list.forEach { categoryRepository.saveCategory(it) }
        return categoryRepository.getCategories().first()
    }

    private suspend fun populateDummyProducts(
        stores: List<Store>, categories: List<Category>, units: List<ProductUnit>
    ): List<Product> {
        val list = listOf(
            Product(
                id = Id.new,
                name = LocalizedString(
                    arName = "عدسات شهرية",
                    enName = "Monthly Lenses"
                ),
                averagePrice = 120.0,
                sellingPrice = 180.0,
                openingBalanceQuantity = 50.0,
                category = categories[0],
                store = stores[1],
                minimumProductUnit = null,
                maximumProductUnit = units[2],
                subUnitsPerMainUnit = 1.0,
                createdAt = Clock.now(),
            ), Product(
                id = Id.new,
                name = LocalizedString(
                    arName = "محلول عدسات",
                    enName = "Lens Solution"
                ),
                averagePrice = 35.5,
                sellingPrice = 55.0,
                openingBalanceQuantity = 100.0,
                category = categories[1],
                store = stores[1],
                minimumProductUnit = null,
                maximumProductUnit = units[0],
                subUnitsPerMainUnit = 1.0,
                createdAt = Clock.now(),
            ), Product(
                id = Id.new,
                name = LocalizedString(
                    arName = "حافظة عدسات",
                    enName = "Lens Case"
                ),
                averagePrice = 5.0,
                sellingPrice = 15.0,
                openingBalanceQuantity = 200.0,
                category = categories[2],
                store = stores[1],
                minimumProductUnit = units[0], // Piece
                maximumProductUnit = units[1], // Dozen
                subUnitsPerMainUnit = 12.0,
                createdAt = Clock.now(),
            )
        )
        list.forEach {
            productRep.saveProduct(it)
        }
        return productRep.getProducts().first()
    }
}
