package com.magic.data.models

data class ChatBotStartConvoBody(
    val statue_name: String,
    val query: String,
    val speak: Boolean,
)