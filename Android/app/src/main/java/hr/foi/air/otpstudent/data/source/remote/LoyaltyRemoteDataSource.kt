package hr.foi.air.otpstudent.data.source.remote

import hr.foi.air.otpstudent.domain.model.Challenge
import hr.foi.air.otpstudent.domain.model.ChallengeState
import hr.foi.air.otpstudent.domain.model.QuizQuestion
import hr.foi.air.otpstudent.domain.model.QuizSubmitResult
import hr.foi.air.otpstudent.domain.model.Reward
import hr.foi.air.otpstudent.domain.model.RewardsFilter

interface LoyaltyRemoteDataSource {
    suspend fun fetchActiveChallenges(): List<Challenge>
    suspend fun fetchChallengeStates(uid: String): List<ChallengeState>
    suspend fun claimChallenge(challengeId: String)

    suspend fun fetchPointsBalance(uid: String): Long

    suspend fun fetchQuizQuestion(challengeId: String): QuizQuestion?

    suspend fun submitQuizAnswer(challengeId: String, selectedIndex: Int): QuizSubmitResult

    //filter stvari
    suspend fun fetchActiveRewards(): List<Reward>

    suspend fun fetchActiveRewards(filter: RewardsFilter?, pointsBalance: Long? = null): List<Reward>

    suspend fun redeemReward(rewardId: String): String
}


