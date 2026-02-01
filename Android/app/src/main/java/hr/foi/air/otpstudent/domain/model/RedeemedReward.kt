package hr.foi.air.otpstudent.domain.model


data class RedeemedReward(
    val rewardId: String,
    val reward: Reward,
    val redemptionId: String
)
