package hr.foi.air.otpstudent.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUi(
    val firstName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val birthday: String = "",
    val gender: String = "",
    val faculty: String = "",
    val educationLevel: String = "",
    val major: String = "",
    val studyYear: String = "",
    val avatarUrl: String = ""
)

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Loaded(val ui: ProfileUi) : ProfileState()
    data class Saved(val message: String = "Podaci spremljeni") : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val state: StateFlow<ProfileState> = _state

    fun load() = viewModelScope.launch {
        val uid = authRepo.currentUserId()
        if (uid == null) {
            _state.value = ProfileState.Error("Niste prijavljeni.")
            return@launch
        }

        _state.value = ProfileState.Loading
        try {
            val data = authRepo.getUserDocument(uid)

            val ui = ProfileUi(
                firstName = data?.get("firstName") as? String ?: "",
                lastName = data?.get("lastName") as? String ?: "",
                fullName = data?.get("fullName") as? String ?: "",
                email = data?.get("email") as? String ?: "",
                phone = data?.get("phone") as? String ?: "",
                location = data?.get("location") as? String ?: "",
                birthday = data?.get("birthday") as? String ?: "",
                gender = data?.get("gender") as? String ?: "",
                faculty = data?.get("faculty") as? String ?: "",
                educationLevel = data?.get("educationLevel") as? String ?: "",
                major = data?.get("major") as? String ?: "",
                studyYear = (data?.get("studyYear")?.toString() ?: ""),
                avatarUrl = data?.get("avatarUrl") as? String ?: ""
            )

            _state.value = ProfileState.Loaded(ui)
        } catch (e: Exception) {
            _state.value = ProfileState.Error(e.message ?: "Greška pri dohvaćanju profila")
        }
    }

    fun save(fields: Map<String, Any?>) = viewModelScope.launch {
        val uid = authRepo.currentUserId()
        if (uid == null) {
            _state.value = ProfileState.Error("Niste prijavljeni.")
            return@launch
        }

        _state.value = ProfileState.Loading
        try {
            authRepo.updateUserFields(uid, fields)
            _state.value = ProfileState.Saved()
        } catch (e: Exception) {
            _state.value = ProfileState.Error(e.message ?: "Greška pri spremanju")
        }
    }

    fun uploadAvatar(imageUri: Uri) = viewModelScope.launch {
        val uid = authRepo.currentUserId()
        if (uid == null) {
            _state.value = ProfileState.Error("Niste prijavljeni.")
            return@launch
        }

        _state.value = ProfileState.Loading
        try {
            val url = authRepo.uploadAvatar(uid, imageUri)
            val current = (state.value as? ProfileState.Loaded)?.ui ?: ProfileUi()
            _state.value = ProfileState.Loaded(current.copy(avatarUrl = url))
        } catch (e: Exception) {
            _state.value = ProfileState.Error(e.message ?: "Greška pri uploadu slike")
        }
    }
}
