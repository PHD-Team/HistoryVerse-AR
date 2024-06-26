package com.magic.data.models

import com.google.gson.annotations.SerializedName

data class BotAnswerDto(
    @SerializedName("Answer")
    val answer: String?,
    @SerializedName("Question")
    val question: String?,
    @SerializedName("audio_url")
    val audioUrl: String?
)