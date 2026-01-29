package hr.foi.air.otpstudent.ui.chat.history

import hr.foi.air.otpstudent.ui.chat.model.ChatConversation

data class ChatHistoryUiState(
    val conversations: List<ChatConversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
