package hr.foi.air.otpstudent.domain.model

data class ChallengeWithState(
    val challenge: Challenge,
    val state: ChallengeState?
)
