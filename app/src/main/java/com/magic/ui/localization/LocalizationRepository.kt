package com.magic.ui.localization

import android.content.Context
import com.magic.ui.localization.models.FireBaseAnchor

class LocalizationRepository  {
    private val fireBase: LocalizationFireBase =  LocalizationFireBase()
   suspend fun getPathAnchorsIds(order:Int,context:Context): List<FireBaseAnchor> {
        return fireBase.getPathAnchorsIds(order,context)
    }

}