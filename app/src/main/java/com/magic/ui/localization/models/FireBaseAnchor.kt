package com.magic.ui.localization.models

data class FireBaseAnchor(
    val anchor: String = "",
    val order: Int = 1,
)
data class FireBasePath(
    val order: Int = 1,
    val anchors: List<FireBaseAnchor> = emptyList()
)
