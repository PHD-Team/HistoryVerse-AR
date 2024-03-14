import com.google.mlkit.vision.objects.DetectedObject

typealias IdAnalyzer = (detectedObject: DetectedObject) -> Unit
typealias LumaListener = (luma: Double) -> Unit

