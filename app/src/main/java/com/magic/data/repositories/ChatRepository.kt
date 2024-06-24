package com.magic.data.repositories

import com.magic.data.models.ChatBotStartConvoBody
import com.magic.data.models.ChatBotVoiceBody

interface ChatRepository {
    suspend fun sendTextToBot(messageBody: ChatBotStartConvoBody): String
    suspend fun sendVoiceToBot(messageBody: ChatBotVoiceBody): String
    suspend fun uploadAudio(audioUri: String): String
}