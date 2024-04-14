package com.magic.ui.fragments

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.magic.data.repositories.MuseMagicRepositoryImpl
import com.magic.ui.R
import com.magic.ui.databinding.FragmentResolveBotBinding
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.light.position
import io.github.sceneview.math.lookAt
import io.github.sceneview.utils.doOnApplyWindowInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch


class ResolveBotFragment : Fragment() {
    private var _binding: FragmentResolveBotBinding? = null
    private val repository = MuseMagicRepositoryImpl()
    private lateinit var cloudAnchorNode: ArModelNode
    private val firstKitAudio = MediaPlayer()
    private val welcomeAudio = MediaPlayer()
    private lateinit var anchorId: String
    private var isLoading = false
        set(value) {
            field = value
            binding.loadingView.isGone = !value
        }
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using view binding
        _binding = FragmentResolveBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val topGuideline = view.findViewById<Guideline>(R.id.topGuideline)
        topGuideline.doOnApplyWindowInsets { systemBarsInsets ->
            // Add the action bar margin
            val actionBarHeight =
                (requireActivity() as AppCompatActivity).supportActionBar?.height ?: 0
            topGuideline.setGuidelineBegin(systemBarsInsets.top + actionBarHeight)
        }
        val bottomGuideline = view.findViewById<Guideline>(R.id.bottomGuideline)
        bottomGuideline.doOnApplyWindowInsets { systemBarsInsets ->
            bottomGuideline.setGuidelineEnd(systemBarsInsets.bottom)
        }
        binding.sceneView.apply {
            planeRenderer.isEnabled = false
            indirectLightEstimated
            planeFindingMode
            isDepthOcclusionEnabled = true
            cloudAnchorEnabled = true
            // Move the instructions up to avoid an overlap with the buttons
        }
        binding.skipButton.setOnClickListener {
            binding.cardView.isVisible = false
            binding.resolveButton.isVisible = true
        }
        lifecycleScope.launch(Dispatchers.Main) {
            binding.skipButton.isVisible = false
            delay(1000)
            playAudio(R.raw.welcome, welcomeAudio)
            delay(2000)
            binding.cardView.isVisible = true
            delay(1000)
            getAnchorId()
            binding.skipButton.isVisible = true
        }
        cloudAnchorNode =
            ArModelNode(
                engine = binding.sceneView.engine,
                placementMode = PlacementMode.PLANE_HORIZONTAL
            ).apply {
                parent = binding.sceneView
                isSmoothPoseEnable = false
                isVisible = false
                loadModelGlbAsync(
                    glbFileLocation = "models/mainModelTempProjects.glb"
                ) {
                    isLoading = false
                }
            }
        binding.resolveButton.setOnClickListener {
            getModel()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        detachAnchor()
    }

    private fun playAudio(resourceId: Int, mediaPlayer: MediaPlayer) {
        mediaPlayer.setDataSource(
            requireContext(),
            Uri.parse("android.resource://com.magic.ui/$resourceId")
        )
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    private fun getModel() {
        lifecycleScope.launch {
            val getAnchorId = launch {
                isLoading = true
                getAnchorId()
            }
            joinAll(getAnchorId)
            launch {
                resolveAnchor()
            }
        }

    }

    private suspend fun resolveAnchor(){
        cloudAnchorNode.resolveCloudAnchor(anchorId) { anchor: Anchor, success: Boolean ->
            if (success) {
                cloudAnchorNode.isVisible = true
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.resolveButton.isVisible = false
                    delay(1000)
                    playAudio(R.raw.first, firstKitAudio)
                }
            } else {
                Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_LONG).show()
                Log.d(
                    "ResolveBotFragment",
                    "Unable to resolve the Cloud Anchor. The Cloud Anchor state is ${anchor.cloudAnchorState}"
                )
            }
        }
    }

    private fun detachAnchor() {
        cloudAnchorNode.detachAnchor()
    }

    private suspend fun getAnchorId() {
        lifecycleScope.launch {
            isLoading = true
            val data = repository.getAnchorList()
            isLoading = data.isEmpty()
            anchorId = data[1].anchor!!
            binding.editText.setText(data[1].anchor)
            Toast.makeText(requireContext(), data[1].anchor, Toast.LENGTH_LONG).show()
        }
    }

}