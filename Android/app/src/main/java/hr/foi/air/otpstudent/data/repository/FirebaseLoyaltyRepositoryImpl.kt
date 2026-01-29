package hr.foi.air.otpstudent.data.repository

import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.data.source.remote.LoyaltyRemoteDataSource
import hr.foi.air.otpstudent.domain.model.ChallengeWithState
import hr.foi.air.otpstudent.domain.model.QuizQuestion
import hr.foi.air.otpstudent.domain.model.QuizSubmitResult
import hr.foi.air.otpstudent.domain.model.Reward
import hr.foi.air.otpstudent.domain.model.RewardsFilter
import hr.foi.air.otpstudent.domain.repository.LoyaltyRepository
import hr.foi.air.otpstudent.domain.model.RedeemedReward

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

    override suspend fun getRewards(): List<Reward> =
        remote.fetchActiveRewards()

    override suspend fun getRewards(
        filter: RewardsFilter?,
        pointsBalance: Long?
    ): List<Reward> =
        remote.fetchActiveRewards(filter, pointsBalance)

    override suspend fun getRedeemedRewardIdsForCurrentUser(): Set<String> {
        val uid = auth.currentUser?.uid ?: return emptySet()
        return remote.fetchRedeemedRewardIds(uid)
    }

    override suspend fun redeemReward(rewardId: String): String {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")
        val redemptionId = remote.redeemReward(rewardId)

        // u bazu da je korisnik preuzeo reward (za maxPerUser: 1)
        runCatching {
            remote.markRewardRedeemed(uid, rewardId, redemptionId)
        }.onFailure { e ->
            android.util.Log.e("REDEEM", "markRewardRedeemed failed", e)
        }

        return redemptionId
    }

    override suspend fun getRedeemedRewardsForCurrentUser(): List<RedeemedReward> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        val entries = remote.fetchRedeemedRewards(uid)

        // dovuci reward docove (jedan po jedan; kasnije možeš optimizirati batchom/cache)
        val rewards = entries.mapNotNull { e ->
            val reward = remote.fetchRewardById(e.rewardId) ?: return@mapNotNull null
            RedeemedReward(reward = reward, redemptionId = e.redemptionId)
        }

        return rewards
    }

}
