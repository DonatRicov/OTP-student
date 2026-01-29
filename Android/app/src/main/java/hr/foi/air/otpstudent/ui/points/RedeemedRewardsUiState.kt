package hr.foi.air.otpstudent.ui.points

import hr.foi.air.otpstudent.domain.model.RedeemedReward

sealed class RedeemedRewardsUiState {
    object Loading : RedeemedRewardsUiState()
    data class Success(val items: List<RedeemedReward>) : RedeemedRewardsUiState()
    data class Error(val message: String) : RedeemedRewardsUiState()
}
