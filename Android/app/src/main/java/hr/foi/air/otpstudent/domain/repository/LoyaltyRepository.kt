package hr.foi.air.otpstudent.domain.repository

import hr.foi.air.otpstudent.domain.model.ChallengeWithState

interface LoyaltyRepository {
    suspend fun getActiveChallengesForCurrentUser(): List<ChallengeWithState>

    suspend fun markChallengeClaimed(challengeId: String)
}
