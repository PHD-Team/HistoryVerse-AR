import android.media.Image
import android.util.Log
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class ObjectDetector(private val image: Image, private val idAnalyzer: IdAnalyzer) {
    private var job: Job? = null

    private val localModel by lazy {
        LocalModel.Builder()
            .setAssetFilePath("1.tflite")
            .build()
    }

    private val options by lazy {
        CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f)
            .setMaxPerObjectLabelCount(3)
            .build()
    }

    private val objectDetector by lazy { ObjectDetection.getClient(options) }

    fun useCustomObjectDetector() {
        if (!Constants.iscRunning) {
            job = CoroutineScope(Dispatchers.IO).launch {
                try {

                    val resultsTask = objectDetector.process(image,0)

                    // Wait for the task to complete
                    val results = resultsTask.result
                    Constants.iscRunning = true
                    Log.e("labels", "${results.size}")

                    results?.forEach {
                        if (it.labels.size > 0) {
                            idAnalyzer(it)
                        }
                    }
                } catch (e: Exception) {
                    Constants.iscRunning = false
                    e.printStackTrace()
                } finally {
                    Constants.iscRunning = false
                }
            }
        }
    }

    fun release() {
        job?.cancel()
    }
}
