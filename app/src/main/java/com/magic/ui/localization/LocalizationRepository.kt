package com.magic.ui.localization

import com.magic.data.models.FireBasePath

class LocalizationRepository {
    private val fireBase : LocalizationFireBase = LocalizationFireBase()
    suspend fun getPathAnchorsIds(order : Int) : FireBasePath {
        val path = fireBase.getPathAnchorsIds(order)
        return path
    }

}