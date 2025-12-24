package hr.foi.air.otpstudent.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state

    private val _effects = Channel<RegisterEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun register(email: String, pass: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repo.register(email, pass)
                val uid = repo.currentUserId()
                if (uid != null) {
                    repo.saveUserDocument(uid, email)
                }
                _state.update { it.copy(isLoading = false) }
                _effects.trySend(RegisterEffect.GoToProfilePersonal)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Greška pri registraciji") }
                _effects.trySend(RegisterEffect.ShowMessage(e.message ?: "Greška pri registraciji"))
            }
        }
    }
}
