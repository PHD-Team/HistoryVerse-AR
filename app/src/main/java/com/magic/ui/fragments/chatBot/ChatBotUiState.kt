package com.magic.ui.fragments.chatBot

data class ChatBotUiState(
    val messageText: String = "",
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val isSendButtonEnabled: Boolean = false,
    val recordedAudio: String = "",
    val isRecording: Boolean = false,
    val audioFileUri: String = "",
    val statueName: String = "alexander the great",
    val isSuccess: Boolean = false,
)
