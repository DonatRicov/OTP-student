package hr.foi.air.otpstudent


import android.net.Uri

/*Interface za bp*/

interface CvRepository {
    suspend fun saveFile(uri: Uri, fileName: String): String?
    suspend fun addCv(cv: CvDocument)
    suspend fun getAllCvs(): List<CvDocument>
    suspend fun deleteCv(cv: CvDocument)
}

