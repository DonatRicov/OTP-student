package hr.foi.air.otpstudent.ui.chat.model

data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)