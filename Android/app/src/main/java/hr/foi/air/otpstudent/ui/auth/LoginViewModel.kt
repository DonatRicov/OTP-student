package hr.foi.air.otpstudent.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.core.auth.SecureCreds
import hr.foi.air.otpstudent.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    private val _effects = Channel<LoginEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun login(email: String, pass: String, onSaveCreds: (String, String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repo.login(email, pass)
                onSaveCreds(email, pass)
                _state.update { it.copy(isLoading = false) }
                _effects.trySend(LoginEffect.GoToSuccess)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Prijava nije uspjela") }
                _effects.trySend(LoginEffect.ShowMessage(e.message ?: "Prijava nije uspjela"))
            }
        }
    }
}
