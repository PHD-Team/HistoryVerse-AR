package com.magic.data.repositories

import com.magic.data.models.ChatBotResponse
import com.magic.data.models.ChatBotStartConvoBody
import com.magic.data.models.ChatBotVoiceBody

interface ChatRepository {
    suspend fun sendTextToBot(messageBody: ChatBotStartConvoBody): ChatBotResponse
    suspend fun sendVoiceToBot(messageBody: ChatBotVoiceBody): ChatBotResponse
    suspend fun uploadAudio(audioUri: String): String
}