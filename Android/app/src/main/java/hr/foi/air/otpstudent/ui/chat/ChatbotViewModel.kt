package hr.foi.air.otpstudent.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.data.chat.ChatbotRepository
import hr.foi.air.otpstudent.ui.chat.model.ChatMessage
import hr.foi.air.otpstudent.ui.chat.model.ChatbotUiState
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatbotViewModel(
    private val repo: ChatbotRepository
) : ViewModel() {

    private val sessionId = UUID.randomUUID().toString()

    private val _state = MutableStateFlow(
        ChatbotUiState(
            messages = listOf(ChatMessage("Bok! Kako ti mogu pomoći?", fromUser = false))
        )
    )
    val state: StateFlow<ChatbotUiState> = _state

    fun sendMessage(raw: String) {
        val text = raw.trim()
        if (text.isBlank()) return

        _state.update {
            it.copy(
                messages = it.messages + ChatMessage(text = text, fromUser = true),
                isSending = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val reply = repo.send(text, sessionId)
                _state.update {
                    it.copy(
                        messages = it.messages + ChatMessage(text = reply, fromUser = false),
                        isSending = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            text = "Greška: ${e.message ?: "nepoznato"}",
                            fromUser = false
                        ),
                        isSending = false,
                        error = e.message
                    )
                }
            }
        }
    }
}
