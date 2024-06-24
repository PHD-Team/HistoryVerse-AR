package com.magic.ui.fragments.chatBot

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magic.data.models.ChatBotStartConvoBody
import com.magic.data.models.ChatBotVoiceBody
import com.magic.data.repositories.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ChatBotUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            Log.d(
                "start chat",
                chatRepository.sendTextToBot(
                    ChatBotStartConvoBody(
                        speak = false,
                        query = state.value.statueName,
                        statue_name = state.value.statueName
                    )
                ),
            )
        }
    }

    fun onSendClick() {
        if (state.value.messageText.isNotEmpty())
            if (state.value.isSendButtonEnabled) {
                _state.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            message = it.messageText,
                            isSentByUser = true
                        ),
                        messageText = "",
                        isLoading = true
                    )
                }
                viewModelScope.launch {
                    chatRepository.sendTextToBot(
                        ChatBotStartConvoBody(
                            query = state.value.messages.last().message,
                            speak = false,
                            statue_name = state.value.statueName
                        ),
                    ).also { response ->
                        _state.update {
                            it.copy(
                                messages = it.messages + ChatMessage(
                                    response, false,
                                ),
                                isSuccess = true,
                                isLoading = false
                            )
                        }
                    }
                }
            }
    }

    fun updateMicState(isEnabled: Boolean) {
        _state.update {
            it.copy(isSendButtonEnabled = isEnabled)
        }
    }

    fun startRecording() {
        _state.update {
            it.copy(isRecording = true)
        }
    }

    fun stopRecording(fileUri: Uri?) {
        _state.update {
            it.copy(isRecording = false, audioFileUri = fileUri!!.toString())
        }
        onAudioMessageSent()
    }

    private fun onAudioMessageSent() {

        _state.update {
            it.copy(
                messages = it.messages + ChatMessage(
                    message = "Audio message sent",
                    isSentByUser = true,
                ),
                isLoading = true
            )
        }
        viewModelScope.launch {
            chatRepository.uploadAudio(state.value.audioFileUri).also { response ->
                chatRepository.sendVoiceToBot(
                    ChatBotVoiceBody(
                        response,
                        false
                    )
                ).also { answer ->
                    _state.update {
                        it.copy(
                            messages = it.messages + ChatMessage(answer, false),
                            isLoading = false
                        )
                    }
                }
            }
        }
//        Log.d("Voice message", state.value.messages.toString())
    }

    fun updateMessageText(text: String) {
        _state.update {
            it.copy(messageText = text)
        }
    }
}