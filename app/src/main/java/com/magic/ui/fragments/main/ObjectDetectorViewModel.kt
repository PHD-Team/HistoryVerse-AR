package com.magic.ui.fragments.main

import com.magic.ui.utils.ObjectDetector
import androidx.lifecycle.ViewModel


class ObjectDetectorViewModel : ViewModel() {
    private var objectDetector: ObjectDetector? = null

    fun setObjectDetector(detector: ObjectDetector) {
        objectDetector = detector
    }

    fun useCustomObjectDetector() {
        objectDetector?.useCustomObjectDetector()
    }

    fun releaseObjectDetector() {
        objectDetector?.release()
    }
}
