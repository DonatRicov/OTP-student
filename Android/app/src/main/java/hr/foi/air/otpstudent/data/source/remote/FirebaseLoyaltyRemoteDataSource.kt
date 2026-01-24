package hr.foi.air.otpstudent.data.source.remote

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.functions
import hr.foi.air.otpstudent.domain.model.Challenge
import hr.foi.air.otpstudent.domain.model.ChallengeState
import hr.foi.air.otpstudent.domain.model.QuizQuestion
import hr.foi.air.otpstudent.domain.model.QuizSubmitResult
import hr.foi.air.otpstudent.domain.model.Reward
import hr.foi.air.otpstudent.domain.model.RewardsFilter
import kotlinx.coroutines.tasks.await

class FirebaseLoyaltyRemoteDataSource(
    private val db: FirebaseFirestore
) : LoyaltyRemoteDataSource {

    override suspend fun fetchActiveChallenges(): List<Challenge> {
        val snap = db.collection("challenges")
            .whereEqualTo("active", true)
            .get()
            .await()

        return snap.documents.map { doc ->
            val type = doc.getString("type") ?: ""

            val claimWindow = doc.getLong("claimWindowDays")
                ?: doc.getLong("claimWindowDay")
                ?: 0L

            val description = when (type.trim().uppercase()) {
                "QUIZ_WEEKLY" -> (doc.getString("quizDescription") ?: "")
                    .trim()
                    .ifBlank { "Riješi kviz i osvoji bodove." }
                else -> (doc.getString("description") ?: "").trim()
            }

            Challenge(
                id = doc.id,
                title = doc.getString("title") ?: "",
                rewardPoints = doc.getLong("rewardPoints") ?: 0L,
                claimWindowDay = claimWindow,
                active = doc.getBoolean("active") ?: true,
                type = type,
                description = description,
                iconKey = doc.getString("iconKey") ?: "default"
            )
        }
    }

    override suspend fun fetchChallengeStates(uid: String): List<ChallengeState> {
        val snap = db.collection("users")
            .document(uid)
            .collection("challengeStates")
            .get()
            .await()

        return snap.documents.map { doc ->
            ChallengeState(
                challengeId = doc.id,
                status = doc.getString("status") ?: "ACTIVE",
                completedAt = doc.getTimestamp("completedAt")?.let { Timestamp(it.seconds, it.nanoseconds) },
                claimDeadlineAt = doc.getTimestamp("claimDeadlineAt")?.let { Timestamp(it.seconds, it.nanoseconds) },
                claimedAt = doc.getTimestamp("claimedAt")?.let { Timestamp(it.seconds, it.nanoseconds) }
            )
        }
    }

    override suspend fun claimChallenge(challengeId: String) {
        val data = hashMapOf("challengeId" to challengeId)
        Firebase.functions
            .getHttpsCallable("claimChallenge")
            .call(data)
            .await()
    }

    override suspend fun fetchPointsBalance(uid: String): Long {
        val doc = db.collection("users").document(uid).get().await()
        return doc.getLong("pointsBalance") ?: 0L
    }

    override suspend fun fetchQuizQuestion(challengeId: String): QuizQuestion? {
        val snap = db.collection("challenges")
            .document(challengeId)
            .collection("quizQuestions")
            .orderBy("order")
            .limit(1)
            .get()
            .await()

        val doc = snap.documents.firstOrNull() ?: return null

        val optionsAny = doc.get("options") as? List<*>
        val options = optionsAny
            ?.mapNotNull { it as? String }
            ?: emptyList()

        val correctIndexLong = doc.getLong("correctIndex") ?: 0L

        return QuizQuestion(
            id = doc.id,
            text = doc.getString("text") ?: "",
            options = options,
            correctIndex = correctIndexLong.toInt(),
            order = doc.getLong("order") ?: 0L
        )
    }

    override suspend fun submitQuizAnswer(challengeId: String, selectedIndex: Int): QuizSubmitResult {
        val data = hashMapOf(
            "challengeId" to challengeId,
            "selectedIndex" to selectedIndex
        )

        val res = Firebase.functions
            .getHttpsCallable("submitQuizResult")
            .call(data)
            .await()

        val map = res.data as? Map<*, *> ?: emptyMap<String, Any>()

        val correct = (map["correct"] as? Boolean) ?: false
        val pointsAwarded = (map["pointsAwarded"] as? Number)?.toLong() ?: 0L

        return QuizSubmitResult(correct = correct, pointsAwarded = pointsAwarded)
    }

    override suspend fun fetchActiveRewards(): List<Reward> {
        return fetchActiveRewards(filter = null, pointsBalance = null)
    }

    override suspend fun fetchActiveRewards(
        filter: RewardsFilter?,
        pointsBalance: Long?
    ): List<Reward> {

        var query = db.collection("rewards")
            .whereEqualTo("active", true)

        when (filter) {
            null -> Unit

            RewardsFilter.CAN_GET -> {
                requireNotNull(pointsBalance) {
                    "pointsBalance is required for CAN_GET filter"
                }
                query = query.whereLessThanOrEqualTo("costPoints", pointsBalance)
            }

            else -> {
                query = query.whereEqualTo("category", filter.name)
            }
        }

        val snap = query.get().await()

        return snap.documents.map { doc ->
            Reward(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                costPoints = doc.getLong("costPoints") ?: 0L,
                active = doc.getBoolean("active") ?: true,
                validDays = doc.getLong("validDays") ?: 7L,
                channel = doc.getString("channel") ?: "BOTH",
                barcodeFormat = doc.getString("barcodeFormat") ?: "QR",
                imageUrl = doc.getString("imageUrl"),
                maxPerUser = doc.getLong("maxPerUser") ?: 0L,
                category = run {
                    val raw = doc.getString("category") ?: RewardsFilter.OPT_REWARDS.name
                    try {
                        RewardsFilter.valueOf(raw)
                    } catch (_: Exception) {
                        RewardsFilter.OPT_REWARDS
                    }
                }
            )
        }
    }

    override suspend fun fetchRedeemedRewardIds(uid: String): Set<String> {
        val snap = db.collection("users")
            .document(uid)
            .collection("redeemedRewards")
            .get()
            .await()

        // doc id = rewardId
        return snap.documents.map { it.id }.toSet()
    }

    override suspend fun markRewardRedeemed(uid: String, rewardId: String, redemptionId: String) {
        val data = hashMapOf(
            "rewardId" to rewardId,
            "redemptionId" to redemptionId,
            "redeemedAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(uid)
            .collection("redeemedRewards")
            .document(rewardId)
            .set(data)
            .await()
    }

    override suspend fun redeemReward(rewardId: String): String {
        val data = hashMapOf<String, Any>("rewardId" to rewardId)
        val res = Firebase.functions
            .getHttpsCallable("redeemReward")
            .call(data)
            .await()

        val map = res.data as Map<*, *>
        return map["redemptionId"] as String
    }
}
