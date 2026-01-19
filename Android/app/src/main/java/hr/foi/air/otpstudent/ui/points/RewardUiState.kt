package hr.foi.air.otpstudent.ui.points
import hr.foi.air.otpstudent.domain.model.Reward

sealed class RewardsUiState {
    object Loading : RewardsUiState()
    data class Success(val rewards: List<Reward>) : RewardsUiState()
    data class Error(val message: String) : RewardsUiState()
}