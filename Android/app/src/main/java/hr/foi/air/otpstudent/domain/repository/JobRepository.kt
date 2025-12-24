package hr.foi.air.otpstudent.domain.repository

import hr.foi.air.otpstudent.domain.model.Job

interface JobRepository {
    suspend fun getJobsForUser(userId: String?): List<Job>
    suspend fun getFavoriteJobsForUser(userId: String): List<Job>
    suspend fun getJobById(jobId: String): Job?
    suspend fun setFavorite(userId: String, jobId: String, favorite: Boolean)
    suspend fun markApplied(userId: String, jobId: String)

    suspend fun isFavorite(userId: String, jobId: String): Boolean
    suspend fun getJobDetailsForUser(userId: String?, jobId: String): Job?

}
