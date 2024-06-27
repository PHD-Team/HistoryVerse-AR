package com.magic.data.network

import com.magic.data.models.BotAnswerDto
import com.magic.data.models.ChatBotStartConvoBody
import com.magic.data.models.ChatBotTextBody
import com.magic.data.models.ChatBotVoiceBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatBotService {
    @POST("start_convo")
    suspend fun sendTextStartToBot(@Body body: ChatBotStartConvoBody): Response<BotAnswerDto>

    @POST("text_convo")
    suspend fun sendTextToBot(@Body body: ChatBotTextBody): Response<BotAnswerDto>

    @POST("voice_convo")
    suspend fun sendVoiceToBot(@Body body: ChatBotVoiceBody): Response<BotAnswerDto>
}