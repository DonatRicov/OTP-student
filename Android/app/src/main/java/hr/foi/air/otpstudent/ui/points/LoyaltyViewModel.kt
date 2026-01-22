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

    private val _quizQuestion = MutableLiveData<QuizQuestion?>()
    val quizQuestion: LiveData<QuizQuestion?> = _quizQuestion

    private val _quizSubmitResult = MutableLiveData<QuizSubmitResult?>()
    val quizSubmitResult: LiveData<QuizSubmitResult?> = _quizSubmitResult

    private val _rewardsState = MutableLiveData<RewardsUiState>()
    val rewardsState: LiveData<RewardsUiState> = _rewardsState

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

    //fun isRewardFilterEnabled(filter: RewardsFilter): Boolean = rewardFilters.contains(filter)

    /*fun setRewardFilter(filter: RewardsFilter, enabled: Boolean) {
        if (enabled) rewardFilters.add(filter) else rewardFilters.remove(filter)
    }*/

    fun getRewardFiltersSnapshot(): Set<RewardsFilter> = rewardFilters.toSet()

    fun setRewardFilters(newFilters: Set<RewardsFilter>) {
        rewardFilters.clear()
        rewardFilters.addAll(newFilters)
    }

    // kad ode s Rewards tab
    fun clearRewardFiltersSilently() {
        rewardFilters.clear()
    }

    // ucita sve
    fun loadRewards() {
        _rewardsState.value = RewardsUiState.Loading
        viewModelScope.launch {
            runCatching {
                val rewards = repo.getRewards()
                val points = repo.getPointsBalanceForCurrentUser()
                rewards to points
            }.onSuccess { (rewards, points) ->
                _points.value = points
                _rewardsState.value = RewardsUiState.Success(rewards)
            }.onFailure {
                _rewardsState.value = RewardsUiState.Error(it.message ?: "Greška")
            }
        }
    }

    // lokalno filtriranje
    fun applyRewardFilters() {
        _rewardsState.value = RewardsUiState.Loading
        viewModelScope.launch {
            runCatching {
                val rewards = repo.getRewards()
                val points = repo.getPointsBalanceForCurrentUser()

                val canGet = rewardFilters.contains(RewardsFilter.CAN_GET)
                val categories = rewardFilters - RewardsFilter.CAN_GET

                val filtered = rewards.filter { r ->
                    val okCanGet = if (canGet) r.costPoints <= points else true
                    val okCategory = if (categories.isEmpty()) true else categories.contains(r.category)
                    okCanGet && okCategory
                }

                filtered to points
            }.onSuccess { (filtered, points) ->
                _points.value = points
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
