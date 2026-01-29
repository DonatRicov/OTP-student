package hr.foi.air.otpstudent.ui.points

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.model.ChallengeWithState
import hr.foi.air.otpstudent.domain.model.QuizQuestion
import hr.foi.air.otpstudent.domain.model.QuizSubmitResult
import hr.foi.air.otpstudent.domain.model.RewardsFilter
import hr.foi.air.otpstudent.domain.repository.LoyaltyRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import hr.foi.air.otpstudent.domain.model.RedeemedReward

sealed class LoyaltyUiState {
    object Loading : LoyaltyUiState()
    data class Success(val items: List<ChallengeWithState>) : LoyaltyUiState()
    data class Error(val message: String) : LoyaltyUiState()
}

sealed class RedeemUiState {
    object Loading : RedeemUiState()
    data class Success(val redemptionId: String) : RedeemUiState()
    data class Error(val message: String) : RedeemUiState()
}

class LoyaltyViewModel(private val repo: LoyaltyRepository) : ViewModel() {

    private val _state = MutableLiveData<LoyaltyUiState>()
    val state: LiveData<LoyaltyUiState> = _state

    private val _points = MutableLiveData<Long>()
    val points: LiveData<Long> = _points

    private val _quizQuestion = MutableLiveData<QuizQuestion?>()
    val quizQuestion: LiveData<QuizQuestion?> = _quizQuestion

    private val _quizSubmitResult = MutableLiveData<QuizSubmitResult?>()
    val quizSubmitResult: LiveData<QuizSubmitResult?> = _quizSubmitResult

    private val _rewardsState = MutableLiveData<RewardsUiState>()
    val rewardsState: LiveData<RewardsUiState> = _rewardsState

    // redeem state za details
    private val _redeemState = MutableLiveData<RedeemUiState?>()
    val redeemState: LiveData<RedeemUiState?> = _redeemState

    // rewardId koje je korisnik vec preuzel
    private val _redeemedRewardIds = MutableLiveData<Set<String>>(emptySet())
    val redeemedRewardIds: LiveData<Set<String>> = _redeemedRewardIds

    fun load() {
        _state.value = LoyaltyUiState.Loading
        viewModelScope.launch {
            runCatching {
                val challenges = repo.getActiveChallengesForCurrentUser()
                val points = repo.getPointsBalanceForCurrentUser()
                challenges to points
            }.onSuccess { (items, points) ->
                _points.value = points

                val filtered = items.filter { item ->
                    val st = item.state?.status?.trim()?.uppercase() ?: "ACTIVE"
                    st != "CLAIMED"
                }

                _state.value = LoyaltyUiState.Success(filtered)
            }.onFailure {
                _state.value = LoyaltyUiState.Error(it.message ?: "Greška")
            }
        }
    }

    fun claim(challengeId: String) {
        _state.value = LoyaltyUiState.Loading
        viewModelScope.launch {
            runCatching { repo.markChallengeClaimed(challengeId) }
                .onSuccess { load() }
                .onFailure {
                    _state.value = LoyaltyUiState.Error(it.message ?: "Greška pri preuzimanju bodova")
                }
        }
    }

    fun loadQuizQuestion(challengeId: String) {
        viewModelScope.launch {
            runCatching { repo.getQuizQuestion(challengeId) }
                .onSuccess { q -> _quizQuestion.value = q }
                .onFailure { _quizQuestion.value = null }
        }
    }

    fun submitQuizAnswer(challengeId: String, selectedIndex: Int) {
        _state.value = LoyaltyUiState.Loading
        viewModelScope.launch {
            runCatching { repo.submitQuizAnswer(challengeId, selectedIndex) }
                .onSuccess { result ->
                    _quizSubmitResult.value = result
                    load()
                }
                .onFailure {
                    _state.value = LoyaltyUiState.Error(it.message ?: "Greška pri slanju kviza")
                }
        }
    }

    fun clearQuizSubmitResult() {
        _quizSubmitResult.value = null
    }

    private val rewardFilters = mutableSetOf<RewardsFilter>()
    private fun hasAnyRewardFilters(): Boolean = rewardFilters.isNotEmpty()

    fun getRewardFiltersSnapshot(): Set<RewardsFilter> = rewardFilters.toSet()

    fun setRewardFilters(newFilters: Set<RewardsFilter>) {
        rewardFilters.clear()
        rewardFilters.addAll(newFilters)
    }

    // kad ode s Rewards tab
    fun clearRewardFiltersSilently() {
        rewardFilters.clear()
    }

    // ucita sve (skriva  koje su preuzete ako maxPerUser: 1)
    fun loadRewards() {
        _rewardsState.value = RewardsUiState.Loading
        viewModelScope.launch {
            runCatching {
                val rewards = repo.getRewards()
                val points = repo.getPointsBalanceForCurrentUser()
                val redeemed = repo.getRedeemedRewardIdsForCurrentUser()
                Triple(rewards, points, redeemed)
            }.onSuccess { (rewards, points, redeemed) ->
                _points.value = points
                _redeemedRewardIds.value = redeemed

                val visibleRewards = rewards.filter { r ->
                    !redeemed.contains(r.id) // sakrij sve koje je korisnik preuzeo
                }

                _rewardsState.value = RewardsUiState.Success(visibleRewards)
            }.onFailure {
                _rewardsState.value = RewardsUiState.Error(it.message ?: "Greška")
            }
        }
    }




    // lokalno filtriranje i skriva preuzete
    fun applyRewardFilters() {
        _rewardsState.value = RewardsUiState.Loading
        viewModelScope.launch {
            runCatching {
                val rewards = repo.getRewards()
                val points = repo.getPointsBalanceForCurrentUser()
                val redeemed = repo.getRedeemedRewardIdsForCurrentUser()

                val canGet = rewardFilters.contains(RewardsFilter.CAN_GET)
                val categories = rewardFilters - RewardsFilter.CAN_GET

                val filtered = rewards.filter { r ->
                    val okCanGet = if (canGet) r.costPoints <= points else true
                    val okCategory = if (categories.isEmpty()) true else categories.contains(r.category)
                    val okNotRedeemed = !redeemed.contains(r.id)

                    okCanGet && okCategory && okNotRedeemed
                }


                Triple(filtered, points, redeemed)
            }.onSuccess { (filtered, points, redeemed) ->
                _points.value = points
                _redeemedRewardIds.value = redeemed
                _rewardsState.value = RewardsUiState.Success(filtered)
            }.onFailure {
                _rewardsState.value = RewardsUiState.Error(it.message ?: "Greška")
            }
        }
    }

    fun redeemReward(rewardId: String) {
        _rewardsState.value = RewardsUiState.Loading
        viewModelScope.launch {
            runCatching {
                repo.redeemReward(rewardId)
            }.onSuccess {
                // oznaci lokalno
                _redeemedRewardIds.value = (_redeemedRewardIds.value ?: emptySet()) + rewardId

                val newPoints = repo.getPointsBalanceForCurrentUser()
                _points.value = newPoints

                if (hasAnyRewardFilters()) {
                    applyRewardFilters()
                } else {
                    loadRewards()
                }
            }.onFailure {
                _rewardsState.value = RewardsUiState.Error(it.message ?: "Sakupljanje nagrade nije uspio")
            }
        }
    }

    // redeem iz RewardDetails i vraca redemptionId po redeemState
    fun redeemRewardFromDetails(rewardId: String) {
        _redeemState.value = RedeemUiState.Loading
        viewModelScope.launch {
            runCatching {
                repo.redeemReward(rewardId)
            }.onSuccess { redemptionId ->
                // lokalno
                _redeemedRewardIds.value = (_redeemedRewardIds.value ?: emptySet()) + rewardId

                val newPoints = repo.getPointsBalanceForCurrentUser()
                _points.value = newPoints

                // refresh reward listu
                if (hasAnyRewardFilters()) applyRewardFilters() else loadRewards()

                _redeemState.value = RedeemUiState.Success(redemptionId)
            }.onFailure {
                _redeemState.value =
                    RedeemUiState.Error(it.message ?: "Sakupljanje nagrade nije uspjelo")
            }
        }
    }

    fun clearRedeemState() {
        _redeemState.value = null
    }

    private val _redeemedRewardsState = MutableLiveData<RedeemedRewardsUiState>()
    val redeemedRewardsState: LiveData<RedeemedRewardsUiState> = _redeemedRewardsState

    fun loadRedeemedRewards() {
        _redeemedRewardsState.value = RedeemedRewardsUiState.Loading
        viewModelScope.launch {
            runCatching { repo.getRedeemedRewardsForCurrentUser() }
                .onSuccess { list ->
                    _redeemedRewardsState.value = RedeemedRewardsUiState.Success(list)
                }
                .onFailure {
                    _redeemedRewardsState.value = RedeemedRewardsUiState.Error(it.message ?: "Greška")
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


