package com.magic.data.repositories

import com.magic.data.models.ChatBotResponse
import com.magic.data.models.ChatBotStartConvoBody
import com.magic.data.models.ChatBotTextBody
import com.magic.data.models.ChatBotVoiceBody

interface ChatRepository {
    suspend fun sendTextStartConvoToBot(messageBody: ChatBotStartConvoBody): ChatBotResponse
    suspend fun sendTextToBot(messageBody: ChatBotTextBody): ChatBotResponse
    suspend fun sendVoiceToBot(messageBody: ChatBotVoiceBody): ChatBotResponse
    suspend fun uploadAudio(audioUri: String): String
}