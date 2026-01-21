package hr.foi.air.otpstudent.ui.chat.model

data class ChatConversation(
    val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val title: String = "",
    val messages: List<ChatMessage> = emptyList()
)
