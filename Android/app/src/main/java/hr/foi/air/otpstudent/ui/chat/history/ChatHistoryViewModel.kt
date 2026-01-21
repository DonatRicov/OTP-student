package hr.foi.air.otpstudent.ui.chat.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.ui.chat.ChatConversationsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatHistoryViewModel(
    private val store: ChatConversationsStore,
    private val historyKey: String
) : ViewModel() {

    private val _state = MutableStateFlow(ChatHistoryUiState())
    val state: StateFlow<ChatHistoryUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val list = store.loadAll(historyKey)

                val filtered = list.filter { it.title.isNotBlank() || it.messages.isNotEmpty() }
                _state.update { it.copy(isLoading = false, conversations = filtered) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Greška") }
            }
        }
    }
}
