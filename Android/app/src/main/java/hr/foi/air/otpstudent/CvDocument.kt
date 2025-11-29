package hr.foi.air.otpstudent

data class CvDocument(
    val id: String,
    val fileName: String,
    val filePath: String, // Local path now, Download URL later
    val uploaderName: String,
    val timestamp: Long
)

