package com.magic.ui.localization

import android.animation.ObjectAnimator
import android.graphics.Path
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.google.ar.core.Config
import com.google.ar.core.Pose
import com.magic.ui.databinding.FragmentLocalizationBinding
import com.magic.ui.localization.models.FireBasePath
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocalizationFragment : Fragment() {
    private var item = 1

    private var cloudNode : ArModelNode? = null
    private val viewModel : LocalizationViewModel by viewModels()
    private var _binding : FragmentLocalizationBinding? = null
    private val binding get() = _binding !!
    private lateinit var sceneView : ArSceneView
    private var isLoading = false
        set(value) {
            field = value
            binding.loadingAnimation.isGone = ! value
        }
    private var path : FireBasePath = FireBasePath()
    private val modelNodes = mutableListOf<ArModelNode>()

    override fun onCreateView(
        inflater : LayoutInflater , container : ViewGroup? ,
        savedInstanceState : Bundle?
    ) : View {
        _binding = FragmentLocalizationBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        sceneView = binding.sceneView.apply {
            arCore.createSession(requireContext())
            configureSession { _ , config ->
                config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
                config.depthMode = Config.DepthMode.AUTOMATIC
                config.focusMode = Config.FocusMode.AUTO
                config.lightEstimationMode = Config.LightEstimationMode.DISABLED
                config.geospatialMode = Config.GeospatialMode.ENABLED

                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
            }
            cloudAnchorEnabled = true
            planeRenderer.isVisible = true
        }

        binding.loadingAnimation.repeatCount = LottieDrawable.INFINITE

        lifecycleScope.launch {
            isLoading = true

            viewModel.getPathAnchors().also { path ->
                this@LocalizationFragment.path = path
            }
            isLoading = false

        }
        binding.readyButton.isVisible = false
        lifecycleScope.launch {
            delay(2500)
            binding.readyButton.isVisible = true
        }
        binding.readyButton.setOnClickListener {
            binding.handPhoneImage.isGone = true
            binding.myText.isGone = true
            binding.readyButton.isGone = true

            isLoading = true
            path.anchors?.first()?.anchor?.let { id -> resolveAnchor(id) }
        }


        sceneView.onArFrame = {
            if (modelNodes.isNotEmpty() &&
                calculateDistance() < 1.6f &&
                item < path.anchors?.size !! &&
                cloudNode?.cloudAnchorTaskInProgress == false
            ) {
                path.anchors?.get(item)?.anchor?.let { id -> resolveAnchor(id) }
                item ++
            } else if (item == path.anchors?.size && calculateDistance() < 1.6f) {
// AUDIO


            }


        }


    }


    private fun resolveAnchor(anchorID : String) {
        lifecycleScope.launch {
            isLoading = true

            cloudNode = ArModelNode(
                engine = sceneView.engine ,
            ).apply {
                placementMode = PlacementMode.BEST_AVAILABLE
                parent = sceneView
                position = Position(0f , 0f , 0f)
                isSmoothPoseEnable = false
                isVisible = true
                loadModelGlbAsync("models/ball.glb" , scaleToUnits = .3f)
            }.apply {
                resolveCloudAnchor(cloudAnchorId = anchorID) { anchor , success ->
                    if (success) {
                        modelNodes.add(this)
                        isLoading = false
                        pose = Pose.IDENTITY
                        isVisible = true
                    } else {
                        isLoading = false
                        Toast.makeText(
                            requireContext() , "failed to resolve" , Toast.LENGTH_LONG
                        ).show()
                    }
                }
                while (cloudAnchorTaskInProgress) {
                    delay(100) // Wait for a short period before checking again
                }
            }
        }
        isLoading = false
    }

    private fun calculateDistance() : Float {
        val cameraPosition = sceneView.cameraNode.position
        val nodePosition = cloudNode?.position ?: return 4f

        val dx = cameraPosition.x - nodePosition.x
        val dy = cameraPosition.y - nodePosition.y
        val dz = cameraPosition.z - nodePosition.z

        return Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    override fun onResume() {
        binding.handPhoneImage.doOnLayout {
            val x = it.x
            val y = it.y
            val animationPath = Path().apply {
                setLastPoint(x , y)
                lineTo(x + x / 2 , y)
                lineTo(x , y - y / 4)
                lineTo(x - x / 2 , y)
                lineTo(x , y)
            }
            val animationDuration = 3000L

            ObjectAnimator.ofFloat(it , View.X , View.Y , animationPath).apply {
                duration = animationDuration
                repeatCount = Animation.INFINITE
                start()
            }
        }
        super.onResume()
    }
}