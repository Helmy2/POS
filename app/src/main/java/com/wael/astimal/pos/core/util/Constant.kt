package com.wael.astimal.pos.core.util

const val PREFERENCES_NAME = "unify.preferences_pb"

const val ORDER_COMMISSION_PERCENTAGE = 0.25
const val RETURN_COMMISSION_PERCENTAGE = 0.25

object ApiRoutes {
    const val HOST = "wael.astimal.com"
    const val BASE_URL = "https://$HOST/api/v1"
    const val PROFILE_IMAGE_BASE_URL = "https://$HOST"
    const val LOGIN = "$BASE_URL/login"
    const val SYNC_UNITS = "https://wael.astimal.com/api/Global/units"
    const val SYNC_EMPLOYEES = "https://wael.astimal.com/api/Global/employees"
    const val SYNC_CLIENTS = "https://wael.astimal.com/api/Global/clients"
    const val SYNC_SUPPLIERS = "https://wael.astimal.com/api/Global/suppliers"
    const val SYNC_CATEGORIES = "https://wael.astimal.com/api/Global/categories"
    const val SYNC_PRODUCTS = "https://wael.astimal.com/api/Global/products"
}

