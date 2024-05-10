package com.magic.ui.localization

import androidx.lifecycle.ViewModel
import com.magic.data.models.FireBasePath
import com.magic.ui.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalizationViewModel : ViewModel() {
    private val repository = LocalizationRepository()
    var order : Int = 1
    private var _path : MutableStateFlow<FireBasePath> = MutableStateFlow(FireBasePath())
    val path = _path.asStateFlow()

    suspend fun getPathAnchors(order:Int) : FireBasePath {
        _path.value = repository.getPathAnchorsIds(order)
        this.order ++
        return path.value
    }
}