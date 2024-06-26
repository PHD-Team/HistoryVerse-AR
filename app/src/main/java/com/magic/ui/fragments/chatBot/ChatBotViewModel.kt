package com.magic.ui.fragments.chatBot

import android.net.Uri
import android.util.Log
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
import javax.inject.Inject

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ChatBotUiState())
    val state = _state.asStateFlow()

    fun onSendClick() {
        if (state.value.messageText.isNotEmpty())
            if (state.value.isSendButtonEnabled) {
                _state.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            message = it.messageText,
                            isSentByUser = true,
                        ),
                        messageText = "",
                        isLoading = true
                    )
                }
                viewModelScope.launch {
                    chatRepository.sendTextToBot(
                        ChatBotStartConvoBody(
                            query = state.value.messages.last().message,
                            speak = true,
                            statue_name = state.value.statueName
                        ),
                    ).also { response ->
                        _state.update {
                            it.copy(
                                messages = it.messages + ChatMessage(
                                    response.answer, false,
                                    voiceUrl = response.voiceUrl
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
                        true
                    )
                ).also { chatResponse ->
                    _state.update {
                        it.copy(
                            messages = it.messages + ChatMessage(
                                chatResponse.answer,
                                false,
                                voiceUrl = chatResponse.voiceUrl
                            ),
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

    fun updateStatueName(statueName: String) {
        _state.update {
            it.copy(statueName = statueName)
        }
        viewModelScope.launch {
            val response = chatRepository.sendTextToBot(
                ChatBotStartConvoBody(
                    speak = false,
                    query = statueName,
                    statue_name = statueName
                )
            )

            Log.d("start chat", response.answer)
        }
    }
}