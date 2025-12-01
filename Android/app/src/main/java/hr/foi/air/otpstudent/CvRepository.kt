package hr.foi.air.otpstudent


import android.net.Uri

/*Interface za baze podataka*/

interface CvRepository {
    // Saves the PDF file and returns the path (or URL) where it is saved
    suspend fun saveFile(uri: Uri, fileName: String): String?

    // Saves the file info (metadata) to the list
    suspend fun addCv(cv: CvDocument)

    // Gets the list of all CVs
    suspend fun getAllCvs(): List<CvDocument>

    // Deletes a CV
    suspend fun deleteCv(cv: CvDocument)
}
