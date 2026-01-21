package hr.foi.air.otpstudent.data.repository

import hr.foi.air.otpstudent.data.source.remote.InternshipDoc
import hr.foi.air.otpstudent.data.source.remote.InternshipsRemoteDataSource
import hr.foi.air.otpstudent.domain.model.Internship
import hr.foi.air.otpstudent.domain.repository.InternshipRepository

class FirebaseInternshipRepositoryImpl(
    private val remote: InternshipsRemoteDataSource
) : InternshipRepository {

    override suspend fun getInternshipsForUser(userId: String?): List<Internship> {
        val docs = remote.fetchInternships()

        if (userId.isNullOrBlank()) {
            return docs.map { it.toDomain(false, false) }
        }

        val appliedIds = remote.fetchAppliedIds(userId)
        val favoriteIds = remote.fetchFavoriteIds(userId)

        return docs.map {
            it.toDomain(
                isApplied = appliedIds.contains(it.id),
                isFavorite = favoriteIds.contains(it.id)
            )
        }
    }

    override suspend fun getAppliedInternshipsForUser(userId: String): List<Internship> {
        val appliedIds = remote.fetchAppliedIds(userId)
        if (appliedIds.isEmpty()) return emptyList()

        val favoriteIds = remote.fetchFavoriteIds(userId)
        val docs = remote.fetchInternships()

        return docs
            .filter { appliedIds.contains(it.id) }
            .map {
                it.toDomain(
                    isApplied = true,
                    isFavorite = favoriteIds.contains(it.id)
                )
            }
    }

    override suspend fun getInternshipById(internshipId: String): Internship? {
        val doc = remote.fetchInternshipById(internshipId) ?: return null
        return doc.toDomain(isApplied = false, isFavorite = false)
    }

    override suspend fun isFavorite(userId: String, internshipId: String): Boolean {
        return remote.fetchFavoriteIds(userId).contains(internshipId)
    }

    override suspend fun setFavorite(userId: String, internshipId: String, favorite: Boolean) {
        remote.setFavorite(userId, internshipId, favorite)
    }

    override suspend fun isApplied(userId: String, internshipId: String): Boolean {
        return remote.fetchAppliedIds(userId).contains(internshipId)
    }

    override suspend fun markApplied(userId: String, internshipId: String) {
        remote.markApplied(userId, internshipId)
    }

    override suspend fun markViewed(userId: String, internshipId: String) {
        remote.markViewed(userId, internshipId)
    }
}

private fun InternshipDoc.toDomain(isApplied: Boolean, isFavorite: Boolean): Internship {
    return Internship(
        id = id,
        title = title,
        company = company,
        location = location,
        hourlyRate = hourlyRate,
        hourlyRateMax = hourlyRateMax,
        applicantsCount = applicantsCount,
        postedAt = postedAt,
        expiresAt = expiresAt,
        isClosed = isClosed,
        isApplied = isApplied,
        isFavorite = isFavorite,
        description = description,
        applyUrl = applyUrl,
        requirements = requirements
    )
}
