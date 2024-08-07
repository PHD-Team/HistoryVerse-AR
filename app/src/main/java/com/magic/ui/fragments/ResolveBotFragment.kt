package com.magic.ui.fragments

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.magic.data.repositories.MuseMagicRepositoryImpl
import com.magic.ui.R
import com.magic.ui.databinding.FragmentResolveBotBinding
import com.magic.ui.fragments.chatBot.ChatBotFragment
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
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
        val sharedPref = requireActivity().getSharedPreferences("path", Context.MODE_PRIVATE)
        val order = sharedPref.getInt("order", 1)

        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            getAnchorId(order)
            binding.resolveButton.isVisible = true
        }
        binding.resolveButton.setOnClickListener {
            getModel(order)
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

    private fun getModel(order: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            val getAnchorId = launch {
                isLoading = true
                getAnchorId(order)
            }
            joinAll(getAnchorId)
            launch(Dispatchers.Main) {
                resolveAnchor(order)
            }
        }

    }

    private suspend fun resolveAnchor(order: Int) {
        cloudAnchorNode =
            ArModelNode(
                engine = sceneView.engine,
            ).apply {
                parent = sceneView
                isSmoothPoseEnable = false
                isVisible = false
                loadModelGlbAsync(
                    glbFileLocation = "models/mainModelTempProjects.glb",
                    scaleToUnits = 1.2f
                ) {
                    isVisible = true
                    isLoading = false
                    position = Position(0f, 3f, 0f)
                }
            }.apply {

                resolveCloudAnchor(anchorId) { anchor: Anchor, success: Boolean ->
                    if (success) {
                        cloudAnchorNode!!.isVisible = true
                        lifecycleScope.launch(Dispatchers.Main) {
                            binding.resolveButton.isVisible = false
                            delay(1000)
                            if (order == 1)
                                playAudio(R.raw.first, firstKitAudio)
                            else
                                playAudio(R.raw.nefirtari, secondKitAudio)
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
//                sharedPref.edit().putInt("order", sharedPref.getInt("order", 1) + 1).apply()
                val fragment = ChatBotFragment()
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.containerFragment, fragment)
                    addToBackStack(null)
                    commit()
                }
            }
        }
    }


    private suspend fun getAnchorId(order: Int) {
        lifecycleScope.launch {
            isLoading = true
            val data = repository.getAnchorList()
            isLoading = data.isEmpty()
            anchorId = data[order].anchor!!
            binding.editText.setText(data[order].anchor)
            Toast.makeText(requireContext(), data[order].anchor, Toast.LENGTH_LONG).show()
        }
    }

}