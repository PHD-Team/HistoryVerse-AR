package com.magic.data.repositories

import com.magic.data.dataSources.ChatBotDataSource
import com.magic.data.models.ChatBotStartConvoBody
import com.magic.data.models.ChatBotVoiceBody
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatBotDataSource: ChatBotDataSource
) : ChatRepository {

    override suspend fun sendTextToBot(messageBody: ChatBotStartConvoBody): String {
        return try {
            chatBotDataSource.sendTextToBot_startConvo(messageBody)
        } catch (e: Exception) {
            "error connecting"
        }
    }

    override suspend fun sendVoiceToBot(messageBody: ChatBotVoiceBody): String {

        return try {

            chatBotDataSource.sendVoiceToBot_startConvo(messageBody)
        } catch (exception: Exception) {
            exception.localizedMessage?.toString().toString()
        }
    }

    override suspend fun uploadAudio(audioUri: String): String =
        chatBotDataSource.uploadAudio(audioUri)


}