package hr.foi.air.otpstudent.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.usecase.GetPushEnabledUseCase
import hr.foi.air.otpstudent.domain.usecase.SetPushEnabledUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getPushEnabled: GetPushEnabledUseCase,
    private val setPushEnabled: SetPushEnabledUseCase
) : ViewModel() {

    private val _pushEnabled = MutableStateFlow(false)
    val pushEnabled: StateFlow<Boolean> = _pushEnabled

    fun load() {
        viewModelScope.launch {
            _pushEnabled.value = getPushEnabled()
        }
    }

    fun onPushToggled(enabled: Boolean) {
        viewModelScope.launch {
            setPushEnabled(enabled)
            _pushEnabled.value = enabled
        }
    }
}
