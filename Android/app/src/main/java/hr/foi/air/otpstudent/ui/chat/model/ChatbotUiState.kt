package hr.foi.air.otpstudent.ui.chat.model

data class ChatbotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null
)
