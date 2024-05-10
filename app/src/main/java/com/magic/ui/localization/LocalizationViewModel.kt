package com.magic.ui.localization

import android.content.Context
import androidx.lifecycle.ViewModel
import com.magic.data.models.FireBasePath
import com.magic.ui.MainActivity
import io.github.sceneview.ar.ArSceneView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocalizationViewModel : ViewModel() {
    private val repository = LocalizationRepository()
    var order: Int = 1
    private var _path: MutableStateFlow<FireBasePath> = MutableStateFlow(FireBasePath())
    val path = _path.asStateFlow()
    private var _sceneView: MutableStateFlow<ArSceneView?> = MutableStateFlow(null)
    val sceneView = _sceneView.asStateFlow()
    fun setSceneView(sceneView: ArSceneView) {
        _sceneView.update {
            sceneView
        }

    }

    suspend fun getPathAnchors(): FireBasePath {
        _path.value = repository.getPathAnchorsIds(1)
        this.order++
        return path.value
    }
}