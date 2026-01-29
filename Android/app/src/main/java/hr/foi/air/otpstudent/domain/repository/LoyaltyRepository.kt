package hr.foi.air.otpstudent.domain.repository

import hr.foi.air.otpstudent.domain.model.ChallengeWithState
import hr.foi.air.otpstudent.domain.model.QuizQuestion
import hr.foi.air.otpstudent.domain.model.QuizSubmitResult
import hr.foi.air.otpstudent.domain.model.Reward
import hr.foi.air.otpstudent.domain.model.RewardsFilter
import hr.foi.air.otpstudent.domain.model.RedeemedReward

interface LoyaltyRepository {
    suspend fun getActiveChallengesForCurrentUser(): List<ChallengeWithState>
    suspend fun markChallengeClaimed(challengeId: String)
    suspend fun getPointsBalanceForCurrentUser(): Long
    suspend fun getQuizQuestion(challengeId: String): QuizQuestion?
    suspend fun submitQuizAnswer(challengeId: String, selectedIndex: Int): QuizSubmitResult

    suspend fun getRewards(): List<Reward>

    // filter
    suspend fun getRewards(
        filter: RewardsFilter?,
        pointsBalance: Long? = null
    ): List<Reward>

    // rewardId koje je trenutni korisnik preuzel
    suspend fun getRedeemedRewardIdsForCurrentUser(): Set<String>

    suspend fun redeemReward(rewardId: String): String

    suspend fun getRedeemedRewardsForCurrentUser(): List<RedeemedReward>

}
