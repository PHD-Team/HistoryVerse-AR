package com.magic.ui.fragments

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.magic.data.repositories.MuseMagicRepositoryImpl
import com.magic.ui.R
import com.magic.ui.databinding.FragmentResolveBotBinding
import com.magic.ui.fragments.main.ModelFragment
import com.magic.ui.localization.LocalizationFragment
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch


class ResolveBotFragment : Fragment() {
    private var _binding: FragmentResolveBotBinding? = null
    private val repository = MuseMagicRepositoryImpl()
    private var cloudAnchorNode: ArModelNode? = null
    private val firstKitAudio = MediaPlayer()
    private val secondKitAudio = MediaPlayer()
    private lateinit var anchorId: String
    private lateinit var sceneView: ArSceneView
    private var isResolved = false
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
        sceneView = binding.sceneView.apply {
            arCore.createSession(requireContext())
            configureSession { _, config ->
                config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
                config.depthMode = Config.DepthMode.AUTOMATIC
                config.focusMode = Config.FocusMode.AUTO
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.geospatialMode = Config.GeospatialMode.ENABLED

                config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
            }
            cloudAnchorEnabled = true
            planeRenderer.isVisible = true
            // Move the instructions up to avoid an overlap with the buttons
        }

        binding.skipButton.setOnClickListener {
            binding.cardView.isVisible = false
            binding.resolveButton.isVisible = true
        }
        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            getAnchorId()
            binding.resolveButton.isVisible = true
        }
        binding.resolveButton.setOnClickListener {
            getModel()
        }
        firstKitAudio.setOnCompletionListener {
            lifecycleScope.launch(Dispatchers.Main) {
                navigateToLocalizationFragment()
            }
        }
        secondKitAudio.setOnCompletionListener {
            lifecycleScope.launch(Dispatchers.Main) {
                navigateToLocalizationFragment()
            }
        }

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
        lifecycleScope.launch(Dispatchers.Main) {
            val getAnchorId = launch {
                isLoading = true
                getAnchorId()
            }
            joinAll(getAnchorId)
            launch(Dispatchers.Main) {
                resolveAnchor()
            }
        }

    }

    private suspend fun resolveAnchor() {
        cloudAnchorNode =
            ArModelNode(
                engine = sceneView.engine,
            ).apply {
                parent = sceneView
                isSmoothPoseEnable = false
                isVisible = false
                loadModelGlbAsync(
                    glbFileLocation = "models/mainModelTempProjects.glb",
                    scaleToUnits = 0.5f
                ) {
                    isVisible = true
                    isLoading = false
                }
            }.apply {

                resolveCloudAnchor(anchorId) { anchor: Anchor, success: Boolean ->
                    if (success) {
                        cloudAnchorNode!!.isVisible = true
                        lifecycleScope.launch(Dispatchers.Main) {
                            binding.resolveButton.isVisible = false
                            delay(1000)
                            playAudio(R.raw.alexander, firstKitAudio)
                            isResolved = true

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
    }

    private suspend fun navigateToLocalizationFragment() {
        if (isResolved && (!firstKitAudio.isPlaying || !secondKitAudio.isPlaying)) {
            lifecycleScope.launch(Dispatchers.Main) {
                val sharedPref = requireActivity().getSharedPreferences(
                    "path",
                    Context.MODE_PRIVATE
                )
                sharedPref.edit().putInt("order", sharedPref.getInt("order", 1) + 1).apply()
                val fragment = LocalizationFragment()
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.containerFragment, fragment)
                    addToBackStack(null)
                    commit()
                }
            }
        }
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