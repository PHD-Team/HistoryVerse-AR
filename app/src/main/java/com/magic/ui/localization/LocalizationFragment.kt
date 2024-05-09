package com.magic.ui.localization

import android.animation.ObjectAnimator
import android.graphics.Path
import android.media.MediaPlayer
import android.net.Uri
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
import com.magic.data.models.FireBasePath
import com.magic.ui.R
import com.magic.ui.fragments.main.ModelFragment
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
    private val mediaPlayer = MediaPlayer()


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
            isLoading = false

            viewModel.getPathAnchors().also { path ->
                this@LocalizationFragment.path = path
            }
        }

        binding.readyButton.isVisible = false
        binding.handPhoneImage.isGone = true
        lifecycleScope.launch {
            delay(1500)
            binding.welcomeCard.isVisible = true
            playAudio(R.raw.welcome, mediaPlayer)
            delay(5000)
            binding.nextButton.isVisible = true
        }
        binding.apply {
            continueButton.setOnClickListener {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.containerFragment , ModelFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }
            readyButton.setOnClickListener {
                handPhoneImage.isGone = true
                myText.isGone = true
                readyButton.isGone = true

                isLoading = true
                path.anchors?.first()?.anchor?.let { id -> resolveAnchor(id) }
            }

            nextButton.setOnClickListener {
                binding.welcomeCard.isGone = true
                lifecycleScope.launch {
                    binding.handPhoneImage.isVisible = true
                    binding.myText.isVisible = true
                    delay(2500)
                    binding.readyButton.isVisible = true
                }
            }


        }



        sceneView.onArFrame = {
//            "${calculateDistance()}${cloudNode?.cloudAnchorTaskInProgress.toString()}".also {
//                binding.anchorId.text = it
//            }

            if (modelNodes.isNotEmpty() &&
                calculateDistance() in 1.70 .. 1.72 &&
                item <= path.anchors?.size !! &&
                item == modelNodes.size + 1
            ) {
                path.anchors?.get(item - 1)?.anchor?.let { id -> resolveAnchor(id) }
//                binding.anchorId.text = item.toString()
            } else if (
                item == path.anchors?.size?.plus(1) &&
                calculateDistance() in 1.70 .. 1.72 &&
                ! mediaPlayer.isPlaying
            ) {
                playAudio(R.raw.congrats_voice , mediaPlayer)
                binding.welcomeCard.isVisible = true
            }
        }
    }

    private fun playAudio(resourceId : Int , mediaPlayer : MediaPlayer) {
        mediaPlayer.setDataSource(
            requireContext() ,
            Uri.parse("android.resource://com.magic.ui/$resourceId")
        )
        mediaPlayer.prepare()
        mediaPlayer.start()
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
        item ++
    }

    private fun calculateDistance() : Float {
        if (modelNodes.isEmpty())
            return 4f
        val cameraPosition = sceneView.cameraNode.position
        val nodePosition = modelNodes.last().position ?: return 4f

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