package com.magic.ui

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.utils.doOnApplyWindowInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var sceneView: ArSceneView
    private lateinit var loadingView: View
    private lateinit var cardView: View
    private lateinit var editText: EditText
    private lateinit var resolveButton: Button
    private lateinit var skipButton: Button
    private lateinit var actionButton: ExtendedFloatingActionButton
    private lateinit var cloudAnchorNode: ArModelNode
    private lateinit var checkPointAnchorNode: ArModelNode
    private val firstKitAudio = MediaPlayer()
    private val welcomeAudio = MediaPlayer()
    private val db = Firebase.firestore
    private var mode = Mode.HOME

    private var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
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
            planeRenderer.isEnabled = false
            sceneView.indirectLightEstimated
            sceneView.planeFindingMode
            sceneView.isDepthOcclusionEnabled = true
            cloudAnchorEnabled = true
            // Move the instructions up to avoid an overlap with the buttons
            instructions.searchPlaneInfoNode.position.y = -0.5f
        }

        loadingView = view.findViewById(R.id.loadingView)

        actionButton = view.findViewById(R.id.actionButton)
        actionButton.setOnClickListener {
            selectMode(Mode.RESOLVE)
            resolveAnchor()
            actionButtonClicked()
        }

        editText = view.findViewById(R.id.editText)
        editText.addTextChangedListener {
            actionButton.isEnabled = !it.isNullOrBlank()
        }

        resolveButton = view.findViewById(R.id.resolveButton)
        resolveButton.setOnClickListener {
            selectMode(Mode.RESOLVE)
        }
        cardView = view.findViewById(R.id.card_view)
        skipButton = view.findViewById(R.id.skip_button)
        skipButton.setOnClickListener {
            cardView.isVisible = false
            actionButton.text = "check point"
            actionButton.isVisible = true
        }
        isLoading = true
        cloudAnchorNode = ArModelNode(placementMode = PlacementMode.PLANE_HORIZONTAL).apply {
            parent = sceneView
            isSmoothPoseEnable = false
            isVisible = false
            loadModelGlbAsync(
                context = requireContext(),
                lifecycle = lifecycle,
                glbFileLocation = "models/mainModelTempProjects.glb",
                scaleToUnits = 0.7f,
                autoAnimate = true,
            ) {
                isLoading = false
            }
        }
        checkPointAnchorNode = ArModelNode(placementMode = PlacementMode.PLANE_HORIZONTAL).apply {
            parent = sceneView
            isSmoothPoseEnable = false
            isVisible = false
            loadModelGlbAsync(
                context = requireContext(),
                lifecycle = lifecycle,
                glbFileLocation = "models/mainModelTempProjects.glb",
                scaleToUnits = 0.7f,
                autoAnimate = true,
            ) {
                isLoading = false
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            delay(2000)
            playAudio(R.raw.welcome, welcomeAudio)
            delay(3000)
            cardView.isVisible = true
        }
    }

    private fun actionButtonClicked() {
        when (mode) {
            Mode.HOME -> {
            }

            Mode.HOST -> {
//                val frame = sceneView.currentFrame ?: return
//
//                if (!cloudAnchorNode.isAnchored) {
//                    cloudAnchorNode.anchor()
//                }
//
//                if (sceneView.arSession?.estimateFeatureMapQualityForHosting(frame.camera.pose) == Session.FeatureMapQuality.INSUFFICIENT) {
//                    Toast.makeText(context, R.string.insufficient_visual_data, Toast.LENGTH_LONG)
//                        .show()
//                    return
//                }
//
//                cloudAnchorNode.hostCloudAnchor { anchor: Anchor, success: Boolean ->
//                    if (success) {
//                        editText.setText(anchor.cloudAnchorId)
//                        selectMode(Mode.RESET)
//                    } else {
//                        Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_LONG).show()
//                        Log.d(
//                            "ZZZZZZZZZZZZ",
//                            "Unable to host the Cloud Anchor. The Cloud Anchor state is ${anchor.cloudAnchorState}"
//                        )
//                        selectMode(Mode.HOST)
//                    }
//                }
//
//                actionButton.apply {
//                    setText(R.string.hosting)
//                    isEnabled = true
//                }
            }

            Mode.RESOLVE -> {
                cloudAnchorNode.resolveCloudAnchor("ua-4982ca6da00dc1c99e1fbd2a0d880f1f") { anchor: Anchor, success: Boolean ->
                    if (success) {
                        cloudAnchorNode.pose = Pose.IDENTITY
                        cloudAnchorNode.isVisible = true
                        lifecycleScope.launch(Dispatchers.Main) {
                            delay(1000)
                            playAudio(R.raw.first, firstKitAudio)
                        }
                        selectMode(Mode.RESET)
                    } else {
                        Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_LONG).show()
                        Log.d(
                            TAG,
                            "Unable to resolve the Cloud Anchor. The Cloud Anchor state is ${anchor.cloudAnchorState}"
                        )
                        selectMode(Mode.RESOLVE)
                    }
                }

                actionButton.apply {
                    setText(R.string.resolving)
                    isEnabled = false
                }
            }

            Mode.RESET -> {
                cloudAnchorNode.detachAnchor()
                selectMode(Mode.HOME)
            }
        }
    }

    private fun selectMode(mode: Mode) {
        this.mode = mode

        when (mode) {
            Mode.HOME -> {
                editText.isVisible = false
                resolveButton.isVisible = true
                actionButton.isVisible = false
                cloudAnchorNode.isVisible = false
            }

            Mode.HOST -> {
                resolveButton.isVisible = false
                actionButton.apply {
                    setIconResource(R.drawable.ic_host)
                    setText(R.string.host)
                    isVisible = true
                    isEnabled = true
                }
                cloudAnchorNode.isVisible = true
            }

            Mode.RESOLVE -> {
                editText.isVisible = true
                resolveButton.isVisible = false
                actionButton.apply {
                    setIconResource(R.drawable.ic_resolve)
                    setText(R.string.resolve)
                    isVisible = true
                    isEnabled = editText.text.isNotEmpty()
                }
            }

            Mode.RESET -> {
                editText.isVisible = true
                actionButton.apply {
                    setIconResource(R.drawable.ic_reset)
                    setText(R.string.reset)
                    isEnabled = true
                }
            }
        }
    }


    private fun resolveAnchor() {
        db.collection("anchors")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                editText.setText(result.documents[1].data.toString())
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
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

    private enum class Mode {
        HOME, HOST, RESOLVE, RESET
    }

    companion object {
        private const val TAG = "MainFragment"
    }
}