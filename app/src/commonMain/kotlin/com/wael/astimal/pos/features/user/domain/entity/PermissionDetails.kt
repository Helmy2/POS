package com.wael.astimal.pos.features.user.domain.entity

import kotlinx.serialization.Serializable


@Serializable
data class PermissionDetails(
    val canView: Boolean = false,
    val canUpdate: Boolean = false,
    val canCreate: Boolean = false,
    val canDelete: Boolean = false
)