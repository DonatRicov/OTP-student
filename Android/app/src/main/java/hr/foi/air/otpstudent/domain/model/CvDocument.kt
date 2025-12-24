package hr.foi.air.otpstudent.domain.model

data class CvDocument(
    val id: String = "",
    val userId: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val uploaderName: String = "",
    val timestamp: Long = 0L
)