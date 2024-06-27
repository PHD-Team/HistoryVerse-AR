package com.magic.data.repositories

import com.magic.data.dataSources.ChatBotDataSource
import com.magic.data.models.ChatBotResponse
import com.magic.data.models.ChatBotStartConvoBody
import com.magic.data.models.ChatBotVoiceBody
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatBotDataSource: ChatBotDataSource
) : ChatRepository {

    override suspend fun sendTextToBot(messageBody: ChatBotStartConvoBody): ChatBotResponse {
        return try {
            val response = chatBotDataSource.sendTextToBot_startConvo(messageBody)
            response ?: ChatBotResponse("error connecting", null)
        } catch (e: Exception) {
            ChatBotResponse("error connecting", null)
        }
    }

    override suspend fun sendVoiceToBot(messageBody: ChatBotVoiceBody): ChatBotResponse {

        return try {
            val response =
                chatBotDataSource.sendVoiceToBot_startConvo(messageBody)
            response ?: ChatBotResponse("error connecting", null)
        } catch (exception: Exception) {

            ChatBotResponse(exception.localizedMessage?.toString().toString(), null)
        }
    }

    override suspend fun uploadAudio(audioUri: String): String =
        chatBotDataSource.uploadAudio(audioUri)


}