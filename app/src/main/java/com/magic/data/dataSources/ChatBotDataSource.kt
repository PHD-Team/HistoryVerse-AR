package com.magic.data.dataSources

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.magic.data.models.BotAnswerDto
import com.magic.data.models.ChatBotResponse
import com.magic.data.models.ChatBotStartConvoBody
import com.magic.data.models.ChatBotVoiceBody
import com.magic.data.network.ChatBotService
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ChatBotDataSource @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val service: ChatBotService,
) {

    suspend fun sendTextToBot_startConvo(requestBody: ChatBotStartConvoBody): ChatBotResponse? {

        val request = service.sendTextToBot(requestBody)
        return if (request.isSuccessful)
            request.body()?.toBotAnswer()
        else throw Exception(request.message())
    }

    suspend fun sendVoiceToBot_startConvo(requestBody: ChatBotVoiceBody): ChatBotResponse? {
        val request = service.sendVoiceToBot(requestBody)
        return if (request.isSuccessful)
            request.body()?.toBotAnswer()
        else throw Exception(request.message())

    }

    suspend fun uploadAudio(audioUri: String): String {
        val storageRef = firebaseStorage.reference.child("${"users audio"}/${UUID.randomUUID()}")
        storageRef.putFile(Uri.parse(audioUri)).await()
        val downloadUrl = storageRef.downloadUrl.await()
        return downloadUrl.toString()
    }

    private fun BotAnswerDto.toBotAnswer(): ChatBotResponse {
        return ChatBotResponse(this.answer ?: "error connecting", this.audioUrl)
    }
}