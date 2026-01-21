package hr.foi.air.otpstudent.domain.repository

import hr.foi.air.otpstudent.domain.model.Internship

interface InternshipRepository {
    suspend fun getInternshipsForUser(userId: String?): List<Internship>
    suspend fun getAppliedInternshipsForUser(userId: String): List<Internship>
    suspend fun getInternshipById(internshipId: String): Internship?
    suspend fun setFavorite(userId: String, internshipId: String, favorite: Boolean)
    suspend fun markApplied(userId: String, internshipId: String)

    suspend fun isFavorite(userId: String, internshipId: String): Boolean
    suspend fun isApplied(userId: String, internshipId: String): Boolean
    suspend fun markViewed(userId: String, internshipId: String)

}
