package hr.foi.air.otpstudent.ui.points

import androidx.lifecycle.*
import hr.foi.air.otpstudent.domain.model.ChallengeWithState
import hr.foi.air.otpstudent.domain.repository.LoyaltyRepository
import kotlinx.coroutines.launch

sealed class LoyaltyUiState {
    object Loading : LoyaltyUiState()
    data class Success(val items: List<ChallengeWithState>) : LoyaltyUiState()
    data class Error(val message: String) : LoyaltyUiState()
}

class LoyaltyViewModel(private val repo: LoyaltyRepository) : ViewModel() {

    private val _state = MutableLiveData<LoyaltyUiState>()
    val state: LiveData<LoyaltyUiState> = _state

    private val _points = MutableLiveData<Long>()
    val points: LiveData<Long> = _points

    fun load() {
        _state.value = LoyaltyUiState.Loading
        viewModelScope.launch {
            runCatching {
                val challenges = repo.getActiveChallengesForCurrentUser()
                val points = repo.getPointsBalanceForCurrentUser()
                challenges to points
            }
                .onSuccess { (items, points) ->
                    _points.value = points

                    val filtered = items.filter { item ->
                        val st = item.state?.status?.trim()?.uppercase() ?: "ACTIVE"
                        st != "CLAIMED"
                    }

                    _state.value = LoyaltyUiState.Success(filtered)
                }
                .onFailure {
                    _state.value = LoyaltyUiState.Error(it.message ?: "Greška")
                }
        }
    }


    fun claim(challengeId: String) {
        _state.value = LoyaltyUiState.Loading
        viewModelScope.launch {
            runCatching { repo.markChallengeClaimed(challengeId) }
                .onSuccess {
                    load()
                }
                .onFailure {
                    _state.value = LoyaltyUiState.Error(it.message ?: "Greška pri preuzimanju bodova")
                }
        }
    }

}


class LoyaltyViewModelFactory(
    private val repo: LoyaltyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoyaltyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoyaltyViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
