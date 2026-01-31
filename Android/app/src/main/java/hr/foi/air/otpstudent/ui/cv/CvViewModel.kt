package hr.foi.air.otpstudent.ui.cv

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.model.CvDocument
import hr.foi.air.otpstudent.domain.repository.CvRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import hr.foi.air.otpstudent.domain.repository.AuthRepository


class CvViewModel(
    private val repo: CvRepository,
    private val authRepo: AuthRepository,
    private val userIdProvider: () -> String?
) : ViewModel() {

    private val _state = MutableStateFlow(CvUiState(isLoading = true))
    val state: StateFlow<CvUiState> = _state

    private val _effects = Channel<CvEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val uid = userIdProvider() ?: throw IllegalStateException("User not logged in")

                val profile = authRepo.getUserProfile(uid)
                val list = repo.getAllCvs().sortedByDescending { it.timestamp }

                _state.update {
                    it.copy(
                        isLoading = false,
                        cvs = list,
                        fullName = profile.fullName,
                        email = profile.email,
                        major = profile.major,
                        location = profile.location,
                        avatarUrl = profile.avatarUrl
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Greška") }
            }
        }
    }


    fun uploadCv(uri: Uri) {
        val uid = userIdProvider() ?: run {
            _effects.trySend(CvEffect.ShowMessage("Molimo prijavite se."))
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isUploading = true, error = null) }
            try {
                val fileName = "cv_${System.currentTimeMillis()}.pdf"
                val downloadUrl = repo.saveFile(uri, fileName)

                if (downloadUrl.isNullOrBlank()) {
                    _effects.trySend(CvEffect.ShowMessage("Greška pri spremanju."))
                    _state.update { it.copy(isUploading = false) }
                    return@launch
                }

                val newCv = CvDocument(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = uid,
                    fileName = fileName,
                    fileUrl = downloadUrl,
                    uploaderName = "Student",
                    timestamp = System.currentTimeMillis()
                )

                repo.addCv(newCv)

                val list = repo.getAllCvs().sortedByDescending { it.timestamp }
                _state.update { it.copy(isUploading = false, cvs = list) }
                _effects.trySend(CvEffect.ShowMessage("Dodan životopis!"))
            } catch (e: Exception) {
                _state.update { it.copy(isUploading = false, error = e.message ?: "Greška") }
            }
        }
    }

    fun deleteCv(cv: CvDocument) {
        viewModelScope.launch {
            try {
                repo.deleteCv(cv)
                val list = repo.getAllCvs().sortedByDescending { it.timestamp }
                _state.update { it.copy(cvs = list) }
                _effects.trySend(CvEffect.ShowMessage("CV obrisan."))
            } catch (e: Exception) {
                _effects.trySend(CvEffect.ShowMessage("Greška pri brisanju CV-a."))
            }
        }
    }
}
