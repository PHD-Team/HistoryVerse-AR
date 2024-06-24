package com.magic.ui.fragments.chatBot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.magic.ui.R

class ChatAdapter(private var chatMessages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER_MESSAGE = 1
        private const val VIEW_TYPE_BOT_MESSAGE = 2
        private const val VIEW_TYPE_USER_VOCE = 3
    }

    override fun getItemViewType(position: Int): Int {
        val message = chatMessages[position]
        return if (message.isSentByUser) {
            if (message.message.startsWith("Audio message sent"))
                VIEW_TYPE_USER_VOCE
            else
                VIEW_TYPE_USER_MESSAGE
        } else VIEW_TYPE_BOT_MESSAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER_MESSAGE -> {
                val view = inflater.inflate(R.layout.item_chat_message_user, parent, false)
                UserMessageViewHolder(view)
            }

            VIEW_TYPE_USER_VOCE -> {
                val view = inflater.inflate(R.layout.item_chat_voice_user, parent, false)
                UserMessageViewHolder(view)
            }

            else -> {
                val view = inflater.inflate(R.layout.item_chat_message_bot, parent, false)
                BotMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        when (holder) {
            is UserMessageViewHolder -> {
                holder.bind(chatMessage)

            }

            is UserVoiceViewHolder -> {
                holder.bind(chatMessage)
            }

            is BotMessageViewHolder -> {
                holder.bind(chatMessage)
            }
        }
    }

    fun updateMessages(newMessages: ChatMessage) {
        chatMessages.add(newMessages)
        notifyItemInserted(chatMessages.lastIndex)
    }

    override fun getItemCount(): Int = chatMessages.size

    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.UserMessageTextView)

        fun bind(chatMessage: ChatMessage) {
            messageTextView.text = chatMessage.message
        }
    }

    class UserVoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.UserMessageTextView)

        fun bind(chatMessage: ChatMessage) {
            messageTextView.text = chatMessage.message
        }
    }

    class BotMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.botMessageTextView)

        fun bind(chatMessage: ChatMessage) {
            messageTextView.text = chatMessage.message
        }
    }
}
