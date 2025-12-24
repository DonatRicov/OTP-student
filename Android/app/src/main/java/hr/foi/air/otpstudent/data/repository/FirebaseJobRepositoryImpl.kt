package hr.foi.air.otpstudent.data.repository

import hr.foi.air.otpstudent.data.source.remote.JobsRemoteDataSource
import hr.foi.air.otpstudent.domain.model.Job
import hr.foi.air.otpstudent.domain.repository.JobRepository

class FirebaseJobRepositoryImpl(
    private val remote: JobsRemoteDataSource
) : JobRepository {

    override suspend fun getJobsForUser(userId: String?): List<Job> {
        val jobDocs = remote.fetchJobs()

        if (userId.isNullOrBlank()) {
            return jobDocs.map { d ->
                d.toDomain(isApplied = false, isFavorite = false)
            }
        }

        val appliedIds = remote.fetchAppliedIds(userId)
        val favoriteIds = remote.fetchFavoriteIds(userId)

        return jobDocs.map { d ->
            d.toDomain(
                isApplied = appliedIds.contains(d.id),
                isFavorite = favoriteIds.contains(d.id)
            )
        }
    }

    override suspend fun getFavoriteJobsForUser(userId: String): List<Job> {
        val favoriteIds = remote.fetchFavoriteIds(userId)
        if (favoriteIds.isEmpty()) return emptyList()

        val appliedIds = remote.fetchAppliedIds(userId)
        val jobDocs = remote.fetchJobs()

        return jobDocs
            .filter { favoriteIds.contains(it.id) }
            .map { d ->
                d.toDomain(
                    isApplied = appliedIds.contains(d.id),
                    isFavorite = true
                )
            }
    }

    override suspend fun getJobById(jobId: String): Job? {
        val doc = remote.fetchJobById(jobId) ?: return null

        return doc.toDomain(isApplied = false, isFavorite = false)
    }

    override suspend fun setFavorite(userId: String, jobId: String, favorite: Boolean) {
        remote.setFavorite(userId, jobId, favorite)
    }

    override suspend fun markApplied(userId: String, jobId: String) {
        remote.markApplied(userId, jobId)
    }

    override suspend fun isFavorite(userId: String, jobId: String): Boolean {
        return remote.isFavorite(userId, jobId)
    }

    override suspend fun getJobDetailsForUser(userId: String?, jobId: String): Job? {
        val doc = remote.fetchJobById(jobId) ?: return null

        if (userId.isNullOrBlank()) {
            return doc.toDomain(isApplied = false, isFavorite = false)
        }

        val fav = remote.isFavorite(userId, jobId)
        val applied = remote.isApplied(userId, jobId)

        return doc.toDomain(isApplied = applied, isFavorite = fav)
    }

}

private fun hr.foi.air.otpstudent.data.source.remote.JobDoc.toDomain(
    isApplied: Boolean,
    isFavorite: Boolean
): Job {
    return Job(
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
