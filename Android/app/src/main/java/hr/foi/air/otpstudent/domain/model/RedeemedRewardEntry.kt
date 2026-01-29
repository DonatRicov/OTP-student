package hr.foi.air.otpstudent.domain.model

import com.google.firebase.Timestamp

data class RedeemedRewardEntry(
    val rewardId: String,
    val redemptionId: String,
    val redeemedAt: Timestamp? = null
)