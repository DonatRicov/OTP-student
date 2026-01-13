package hr.foi.air.otpstudent.data.repository

import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.data.source.remote.LoyaltyRemoteDataSource
import hr.foi.air.otpstudent.domain.model.ChallengeWithState
import hr.foi.air.otpstudent.domain.repository.LoyaltyRepository

class FirebaseLoyaltyRepositoryImpl(
    private val auth: FirebaseAuth,
    private val remote: LoyaltyRemoteDataSource
) : LoyaltyRepository {

    override suspend fun getActiveChallengesForCurrentUser(): List<ChallengeWithState> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val challenges = remote.fetchActiveChallenges()
        val statesById = remote.fetchChallengeStates(uid).associateBy { it.challengeId }

        return challenges.map { ch ->
            ChallengeWithState(
                challenge = ch,
                state = statesById[ch.id]
            )
        }
    }

    override suspend fun markChallengeClaimed(challengeId: String) {
        val uid = auth.currentUser?.uid ?: return
        remote.updateChallengeStateClaimed(uid, challengeId)
    }
}
