package com.magic.ui.fragments.main

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Guideline
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.magic.data.repositories.MuseMagicRepositoryImpl
import com.magic.ui.R
import com.magic.ui.databinding.FragmentModelBinding
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.light.position
import io.github.sceneview.utils.doOnApplyWindowInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ModelFragment : Fragment(R.layout.fragment_model) {
    private var _binding: FragmentModelBinding? = null
    private val repository = MuseMagicRepositoryImpl()
    private lateinit var sceneView: ArSceneView
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
        _binding = FragmentModelBinding.inflate(inflater, container, false)
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
            // Add the navigation bar margin
            bottomGuideline.setGuidelineEnd(systemBarsInsets.bottom)
        }

        sceneView = view.findViewById(R.id.sceneView)
        sceneView.apply {
            cloudAnchorEnabled = true
        }

        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            playAudio(R.raw.welcome, welcomeAudio)
            delay(2000)
            binding.resolveButton.isVisible = true
            delay(1000)
            getAnchorId()
        }
        cloudAnchorNode =
            ArModelNode(
                engine = sceneView.engine,
                placementMode = PlacementMode.PLANE_HORIZONTAL
            ).apply {
                parent = sceneView
                isSmoothPoseEnable = false
                isVisible = false
                loadModelGlbAsync(
                    glbFileLocation = "mainModelTempProjects.glb",
                    scaleToUnits = 0.7f
                ) {
                    isLoading = false
                }
            }
        binding.resolveButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                resolveAnchor()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        cloudAnchorNode.resolveCloudAnchor("ua-a6541ae534a65caf30a9b29a5f62cd0d") { anchor: Anchor, success: Boolean ->
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