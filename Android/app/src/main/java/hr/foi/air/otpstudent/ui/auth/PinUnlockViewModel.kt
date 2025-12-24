package hr.foi.air.otpstudent.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinUnlockViewModel(
    private val authRepo: AuthRepository,
    private val getSavedCreds: () -> SavedCreds?,
    private val clearSavedCreds: () -> Unit,
    private val verifyPin: (String) -> Boolean
) : ViewModel() {

    private val _state = MutableStateFlow(PinUnlockUiState())
    val state: StateFlow<PinUnlockUiState> = _state

    private val _effects = Channel<PinUnlockEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onNotYouClicked() {
        clearSavedCreds()
        _effects.trySend(PinUnlockEffect.GoToLogin)
    }

    fun onPinEntered(pin: String) {
        val creds = getSavedCreds()
        if (creds == null || creds.email.isBlank() || creds.pass.isBlank()) {
            _effects.trySend(PinUnlockEffect.GoToLogin)
            return
        }

        if (!verifyPin(pin)) {
            _effects.trySend(PinUnlockEffect.ShowMessage("Neispravan PIN"))
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                authRepo.login(creds.email, creds.pass)
                _state.update { it.copy(isLoading = false) }
                _effects.trySend(PinUnlockEffect.GoToMain)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Prijava nije uspjela") }
                _effects.trySend(PinUnlockEffect.ShowMessage(e.message ?: "Prijava nije uspjela"))
            }
        }
    }
}

data class SavedCreds(val email: String, val pass: String)
