package hr.foi.air.otpstudent.data.repository

import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.data.source.remote.LoyaltyRemoteDataSource
import hr.foi.air.otpstudent.domain.model.ChallengeWithState
import hr.foi.air.otpstudent.domain.repository.LoyaltyRepository
import hr.foi.air.otpstudent.domain.model.QuizQuestion
import hr.foi.air.otpstudent.domain.model.QuizSubmitResult

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
        auth.currentUser?.uid ?: return 
        remote.claimChallenge(challengeId)
    }

    override suspend fun getPointsBalanceForCurrentUser(): Long {
        val uid = auth.currentUser?.uid ?: return 0L
        return remote.fetchPointsBalance(uid)
    }


    override suspend fun getQuizQuestion(challengeId: String): QuizQuestion? {
        return remote.fetchQuizQuestion(challengeId)
    }

    override suspend fun submitQuizAnswer(challengeId: String, selectedIndex: Int): QuizSubmitResult {
        auth.currentUser?.uid ?: return QuizSubmitResult(correct = false, pointsAwarded = 0L)
        return remote.submitQuizAnswer(challengeId, selectedIndex)
    }

}
