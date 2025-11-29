package hr.foi.air.otpstudent

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MockCvRepository(private val context: Context) : CvRepository {

    private val sharedPrefs = context.getSharedPreferences("mock_cv_db", Context.MODE_PRIVATE)
    private val gson = Gson()

    override suspend fun saveFile(uri: Uri, fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            // kopiranje iz downloads
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, fileName)

            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return@withContext file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    override suspend fun addCv(cv: CvDocument) {
        val list = getListFromPrefs().toMutableList()
        list.add(cv)
        saveListToPrefs(list)
    }

    override suspend fun getAllCvs(): List<CvDocument> {
        return getListFromPrefs()
    }

    override suspend fun deleteCv(cv: CvDocument) {
        val file = File(cv.filePath)
        if (file.exists()) file.delete()

        val list = getListFromPrefs().toMutableList()
        list.removeAll { it.id == cv.id }
        saveListToPrefs(list)
    }

    //Helper functions for SharedPreferences
    private fun getListFromPrefs(): List<CvDocument> {
        val json = sharedPrefs.getString("cv_list", null) ?: return emptyList()
        val type = object : TypeToken<List<CvDocument>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveListToPrefs(list: List<CvDocument>) {
        sharedPrefs.edit().putString("cv_list", gson.toJson(list)).apply()
    }
}
