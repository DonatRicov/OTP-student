package hr.foi.air.otpstudent.data.source.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class InternshipsRemoteDataSource(
    private val db: FirebaseFirestore
) {
    suspend fun fetchInternships(): List<InternshipDoc> {
        val snap = db.collection("internships")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return snap.documents.map { doc ->
            InternshipDoc(
                id = doc.id,
                title = doc.getString("title") ?: "",
                company = doc.getString("company") ?: "",
                location = doc.getString("location") ?: "",
                hourlyRate = doc.getDouble("hourlyRate") ?: 0.0,
                hourlyRateMax = doc.getDouble("hourlyRateMax") ?: 0.0,
                applicantsCount = (doc.getLong("applicantsCount") ?: 0L).toInt(),
                postedAt = doc.getTimestamp("postedAt"),
                expiresAt = doc.getTimestamp("expiresAt"),
                isClosed = doc.getBoolean("isClosed") ?: false,
                description = doc.getString("description") ?: "",
                applyUrl = doc.getString("applyUrl") ?: "",
                requirements = (doc.get("requirements") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }

    suspend fun fetchInternshipById(id: String): InternshipDoc? {
        val doc = db.collection("internships").document(id).get().await()
        if (!doc.exists()) return null

        return InternshipDoc(
            id = doc.id,
            title = doc.getString("title") ?: "",
            company = doc.getString("company") ?: "",
            location = doc.getString("location") ?: "",
            hourlyRate = doc.getDouble("hourlyRate") ?: 0.0,
            hourlyRateMax = doc.getDouble("hourlyRateMax") ?: 0.0,
            applicantsCount = (doc.getLong("applicantsCount") ?: 0L).toInt(),
            postedAt = doc.getTimestamp("postedAt"),
            expiresAt = doc.getTimestamp("expiresAt"),
            isClosed = doc.getBoolean("isClosed") ?: false,
            description = doc.getString("description") ?: "",
            applyUrl = doc.getString("applyUrl") ?: "",
            requirements = (doc.get("requirements") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )
    }

    suspend fun fetchAppliedIds(userId: String): Set<String> {
        val snap = db.collection("users")
            .document(userId)
            .collection("appliedInternships")
            .get()
            .await()
        return snap.documents.map { it.id }.toHashSet()
    }

    suspend fun fetchFavoriteIds(userId: String): Set<String> {
        val snap = db.collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .await()
        return snap.documents.map { it.id }.toHashSet()
    }

    suspend fun setFavorite(userId: String, internshipId: String, favorite: Boolean) {
        val ref = db.collection("users").document(userId)
            .collection("favorites")
            .document(internshipId)

        if (favorite) ref.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
        else ref.delete().await()
    }

    suspend fun markApplied(userId: String, internshipId: String) {
        val ref = db.collection("users").document(userId)
            .collection("appliedInternships")
            .document(internshipId)

        ref.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
    }
    suspend fun markViewed(userId: String, internshipId: String) {
        val ref = db.collection("users")
            .document(userId)
            .collection("viewedInternships")
            .document(internshipId)

        ref.set(mapOf("createdAt" to FieldValue.serverTimestamp())).await()
    }
}

data class InternshipDoc(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val hourlyRate: Double,
    val hourlyRateMax: Double,
    val applicantsCount: Int,
    val postedAt: com.google.firebase.Timestamp?,
    val expiresAt: com.google.firebase.Timestamp?,
    val isClosed: Boolean,
    val description: String,
    val applyUrl: String,
    val requirements: List<String>
)
