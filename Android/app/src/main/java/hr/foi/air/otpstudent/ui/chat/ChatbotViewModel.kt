package hr.foi.air.otpstudent.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.data.chat.ChatbotRepository
import hr.foi.air.otpstudent.ui.chat.model.ChatConversation
import hr.foi.air.otpstudent.ui.chat.model.ChatMessage
import hr.foi.air.otpstudent.ui.chat.model.ChatbotUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatbotViewModel(
    private val repo: ChatbotRepository,
    private val store: ChatConversationsStore,
    private val historyKey: String,
    private val initialConversationId: String?
) : ViewModel() {

    private var activeConversationId: String

    private val _state = MutableStateFlow(ChatbotUiState())
    val state: StateFlow<ChatbotUiState> = _state

    init {
        val id = initialConversationId?.takeIf { it.isNotBlank() }
        val loaded = if (id != null) store.getById(historyKey, id) else null

        if (loaded != null) {
            activeConversationId = loaded.id
            _state.value = ChatbotUiState(messages = loaded.messages)
        } else {
            val conv = store.createNew(historyKey)
            activeConversationId = conv.id
            _state.value = ChatbotUiState(
                messages = listOf(ChatMessage("Bok! Kako ti mogu pomoći?", fromUser = false))
            )
            persist()
        }
    }


    private fun persist(titleIfEmpty: String? = null) {
        val existing = store.getById(historyKey, activeConversationId)
            ?: ChatConversation(id = activeConversationId)

        val newTitle =
            if (existing.title.isBlank() && !titleIfEmpty.isNullOrBlank()) titleIfEmpty
            else existing.title

        store.upsert(
            historyKey,
            existing.copy(
                title = newTitle,
                messages = _state.value.messages
            )
        )
    }

    fun sendMessage(raw: String, sessionId: String) {
        val text = raw.trim()
        if (text.isBlank()) return

        _state.update {
            it.copy(
                messages = it.messages + ChatMessage(text = text, fromUser = true),
                isSending = true,
                error = null
            )
        }
        // prva user poruka postaje naslov razgovora
        persist(titleIfEmpty = text)

        viewModelScope.launch {
            try {
                val reply = repo.send(text, sessionId)
                _state.update {
                    it.copy(
                        messages = it.messages + ChatMessage(text = reply, fromUser = false),
                        isSending = false
                    )
                }
                persist()
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
                persist()
            }
        }
    }

    fun clearAllHistoryForThisUser() {
        store.clearAll(historyKey)
        val conv = store.createNew(historyKey)
        activeConversationId = conv.id
        _state.value = ChatbotUiState(messages = listOf(ChatMessage("Bok! Kako ti mogu pomoći?", fromUser = false)))
        persist()
    }
}
