package hr.foi.air.core.auth

data class AuthRequest(
    val email: String? = null,
    val extra: Map<String, Any?> = emptyMap()
)

sealed class AuthResult {
    data class Success(val userId: String? = null) : AuthResult()
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult()
    data object Cancelled : AuthResult()
}

data class AuthUiSpec(
    val id: String,
    val title: String,
    val order: Int = 100,
    val iconRes: Int? = null
)
