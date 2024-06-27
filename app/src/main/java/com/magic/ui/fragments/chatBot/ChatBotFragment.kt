package com.magic.ui.fragments.chatBot

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.magic.ui.R
import com.magic.ui.databinding.FragmentChatBotBinding
import com.magic.ui.localization.LocalizationFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ChatBotFragment @Inject constructor() : Fragment() {
    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatBotViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()

        chatAdapter = ChatAdapter(viewModel.state.value.messages.toMutableList())

        chatAdapter.setonItemClickListener(
            object : ChatAdapter.onPlayAudioClickListerner {
                override fun onPlayAudioClick(position: Int) {
                    val voiceUrl = viewModel.state.value.messages[position].voiceUrl
                    if (voiceUrl != null) {
                        onAudioPress(voiceUrl)
                    }
                }
            }
        )
        setUpRecycler()
        binding.editGchatMessage.doOnTextChanged { text, start, before, count ->
            if (!text.isNullOrEmpty()) {
                binding.button.setImageResource(R.drawable.send_icon)
                viewModel.updateMicState(true)
                viewModel.updateMessageText(text.toString())
            } else {
                viewModel.updateMicState(false)
            }
        }
        val sharedPref = requireActivity().getSharedPreferences("path", MODE_PRIVATE)
        val order = sharedPref.getInt("order", 1)

        viewModel.updateStatueName(
            when (order) {
                1 -> "alexander the great"
                2 -> "nefertiti"
                3 -> "Tutankhamun"
                else -> "alexander the great"
            }
        )

        lifecycleScope.launch {
            viewModel.state
            viewModel.state.collect { uiState ->
                if (uiState.isLoading) {
                    binding.progressBar.isGone = false
                } else {
                    binding.progressBar.isGone = true
                }
//                if (uiState.isSuccess)
//                    playAudio(uiState.messageText)

                if (binding.recyclerView.adapter?.itemCount!! < viewModel.state.value.messages.size) {
                    addMessage()
                }

                if (uiState.isRecording) {
                    binding.button.setImageResource(R.drawable.x_icon)
                } else if (!uiState.isSendButtonEnabled) {
                    binding.button.setImageResource(R.drawable.mic_microphone_icon) // Replace with your mic icon
                }
            }
        }
        binding.button.setOnClickListener {
            if (viewModel.state.value.isRecording) {
                stopRecording()
            } else if (viewModel.state.value.isSendButtonEnabled) {
                if (viewModel.state.value.isLoading)
                    return@setOnClickListener
                viewModel.onSendClick()
                binding.editGchatMessage.apply {
                    setText("")
                    clearFocus()
                }
            } else {
                startRecording()

            }
        }

//        playAudio("https://firebasestorage.googleapis.com/v0/b/historyversechatbot.appspot.com/o/speak_audio_file%2F11755b74-f1d0-4581-b1fa-30d24891206c.mp3?alt=media")
        binding.exitButton.setOnClickListener {
            navigateToLocalizationFragment()
        }
    }

    private fun onAudioPress(url: String) {
        if (mediaPlayer == null) {
            playAudio(url)
        } else {
            stopPlayingAudio()
        }
    }

    private fun stopPlayingAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
                release()
            }
        }
        mediaPlayer = null
    }

    private fun playAudio(url: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener { start() }
            setOnCompletionListener {
                release()
                mediaPlayer = null
            }
            prepareAsync()
        }
    }

    private fun navigateToLocalizationFragment() {
        val sharedPref = requireActivity().getSharedPreferences(
            "path",
            MODE_PRIVATE
        )
        sharedPref.edit().putInt("order", sharedPref.getInt("order", 1) + 1).apply()
        val fragment = LocalizationFragment()
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.containerFragment, LocalizationFragment())
            addToBackStack(null)
            commit()
        }

    }

    private fun addMessage() {
        val position = viewModel.state.value.messages.size - 1
        chatAdapter.updateMessages(viewModel.state.value.messages.last())
        binding.recyclerView.smoothScrollToPosition(position)
    }

    private fun setUpRecycler() {

        binding.recyclerView.adapter = chatAdapter
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecording() {
        val timeStamp: String = ""
//            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "museMagicAudio$timeStamp.3gp"
        audioFile = File(requireContext().getExternalFilesDir(null), fileName)
        mediaRecorder = MediaRecorder(requireContext()).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }
        viewModel.startRecording()
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        viewModel.stopRecording(audioFile?.toUri())
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }


    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val SAMPLE_RATE = 16000
        private const val CHANNELS = 1
    }
}