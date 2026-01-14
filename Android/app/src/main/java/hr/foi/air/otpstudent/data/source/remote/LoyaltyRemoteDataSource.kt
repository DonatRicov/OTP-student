package hr.foi.air.otpstudent.data.source.remote

import hr.foi.air.otpstudent.domain.model.Challenge
import hr.foi.air.otpstudent.domain.model.ChallengeState

interface LoyaltyRemoteDataSource {
    suspend fun fetchActiveChallenges(): List<Challenge>
    suspend fun fetchChallengeStates(uid: String): List<ChallengeState>
    suspend fun claimChallenge(challengeId: String)

    suspend fun fetchPointsBalance(uid: String): Long

}
