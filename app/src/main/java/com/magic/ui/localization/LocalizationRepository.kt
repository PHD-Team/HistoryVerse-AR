package com.magic.ui.localization

import com.magic.ui.localization.models.FireBasePath

class LocalizationRepository {
    private val fireBase : LocalizationFireBase = LocalizationFireBase()
    suspend fun getPathAnchorsIds(order : Int) : FireBasePath {
        val path = fireBase.getPathAnchorsIds(order)
        return path
    }

}