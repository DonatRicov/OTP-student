package hr.foi.air.otpstudent.domain.model

data class QuizQuestion(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val order: Long = 0L
)
