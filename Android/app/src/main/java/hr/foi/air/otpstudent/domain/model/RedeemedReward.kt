package hr.foi.air.otpstudent.domain.model


data class RedeemedReward(
    val reward: Reward,
    val redemptionId: String
)