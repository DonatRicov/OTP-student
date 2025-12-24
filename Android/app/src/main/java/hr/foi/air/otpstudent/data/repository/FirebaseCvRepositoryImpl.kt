package hr.foi.air.otpstudent.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hr.foi.air.otpstudent.domain.model.CvDocument
import hr.foi.air.otpstudent.domain.repository.CvRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseCvRepositoryImpl(
    private val userId: String
) : CvRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    override suspend fun saveFile(uri: Uri, fileName: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val fileRef = storageRef
                    .child("cvs")
                    .child(userId)
                    .child(fileName)

                fileRef.putFile(uri).await()
                fileRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("FirebaseCvRepository", "Error uploading CV", e)
                null
            }
        }


    override suspend fun addCv(cv: CvDocument) {
        firestore.collection("users")
            .document(userId)
            .collection("cvs")
            .document(cv.id)
            .set(cv.copy(userId = userId))
            .await()
    }

    override suspend fun getAllCvs(): List<CvDocument> {
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("cvs")
            .get()
            .await()

        return snapshot.toObjects(CvDocument::class.java)
    }

    override suspend fun deleteCv(cv: CvDocument) {
        // Brisanje
        firestore.collection("users")
            .document(userId)
            .collection("cvs")
            .document(cv.id)
            .delete()
            .await()

        try {
            storageRef.child("cvs")
                .child(userId)
                .child(cv.fileName)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}