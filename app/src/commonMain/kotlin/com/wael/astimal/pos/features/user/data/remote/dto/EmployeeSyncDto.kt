package com.wael.astimal.pos.features.user.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.data.remote.dto.TranslationDto
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.domain.entity.UserType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class EmployeeSyncData(
    val employees: List<EmployeeSyncDto>,
    @SerialName("date")
    val nextSyncDate: String
)

@Serializable
data class EmployeeSyncDto(
    val id: Long,
    val name: String,
    @SerialName("is_admin")
    val isAdmin: Int,
    @SerialName("is_employee")
    val isEmployee: Int,
    @SerialName("user_name")
    val userName: String,
    val email: String?,
    val avatar: String?,
    val phone: String?,
    @SerialName("is_block")
    val isBlock: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("employee_store_data")
    val storeData: EmployeeStoreDataDto?
)

@Serializable
data class EmployeeStoreDataDto(
    @SerialName("store_id")
    val storeId: Long,
    @SerialName("store_data")
    val storeInfo: StoreInfoDataDto
)

@Serializable
data class StoreInfoDataDto(
    val id: Long,
    val type: String,
    val name: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val translations: List<TranslationDto>
)


fun EmployeeSyncDto.toEntities(): Pair<UserEntity, StoreEntity?> {
    val role = when {
        this.isAdmin == 1 -> UserType.ADMIN
        this.isEmployee == 1 -> UserType.EMPLOYEE
        else -> UserType.EMPLOYEE
    }

    val userEntity = UserEntity(
        id = this.id,
        name = this.userName,
        enName = this.name,
        arName = this.name,
        email = this.email,
        phone = this.phone,
        isSynced = true,
        createdAt = this.createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = this.updatedAt.parseIsoTimestamp() ?: Clock.now(),
        avatarUrl = avatar,
        userType = role
    )

    val storeEntity = this.storeData?.storeInfo?.let { storeInfo ->
        val arName = storeInfo.translations.find { it.locale == "ar" }?.name
        val enName = storeInfo.translations.find { it.locale == "en" }?.name

        StoreEntity(
            serverId = storeInfo.id,
            arName = arName ?: "",
            enName = enName ?: storeInfo.name,
            type = if (storeInfo.type == "sub") StoreType.SUB else StoreType.MAIN,
            updatedAt = storeInfo.updatedAt.parseIsoTimestamp() ?: Clock.now(),
            createdAt = storeInfo.createdAt.parseIsoTimestamp() ?: Clock.now()
        )
    }


    return userEntity to storeEntity
}
