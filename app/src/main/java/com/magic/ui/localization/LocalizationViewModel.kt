package com.magic.ui.localization

import androidx.lifecycle.ViewModel
import com.magic.ui.localization.models.FireBasePath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalizationViewModel : ViewModel() {
    private val repository = LocalizationRepository()
    var order : Int = 1
    private var _path : MutableStateFlow<FireBasePath> = MutableStateFlow(FireBasePath())
    val path = _path.asStateFlow()

    suspend fun getPathAnchors() : FireBasePath {
        _path.value = repository.getPathAnchorsIds(1 )
        this.order ++
        return path.value
    }
}