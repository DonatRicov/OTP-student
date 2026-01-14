package hr.foi.air.otpstudent.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.domain.model.Challenge
import hr.foi.air.otpstudent.domain.model.ChallengeState
import kotlinx.coroutines.tasks.await
import com.google.firebase.functions.functions
import com.google.firebase.Firebase

class FirebaseLoyaltyRemoteDataSource(
    private val db: FirebaseFirestore
) : LoyaltyRemoteDataSource {

    override suspend fun fetchActiveChallenges(): List<Challenge> {
        val snap = db.collection("challenges")
            .whereEqualTo("active", true)
            .get()
            .await()

        return snap.documents.map { doc ->
            Challenge(
                id = doc.id,
                title = doc.getString("title") ?: "",
                rewardPoints = doc.getLong("rewardPoints") ?: 0L,
                claimWindowDay = doc.getLong("claimWindowDay") ?: 0L,
                active = doc.getBoolean("active") ?: true,
                type = doc.getString("type") ?: ""
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

}
