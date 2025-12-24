package hr.foi.air.otpstudent.domain.repository

import android.net.Uri
import hr.foi.air.otpstudent.domain.model.CvDocument

interface CvRepository {
    suspend fun saveFile(uri: Uri, fileName: String): String?
    suspend fun addCv(cv: CvDocument)
    suspend fun getAllCvs(): List<CvDocument>
    suspend fun deleteCv(cv: CvDocument)
}