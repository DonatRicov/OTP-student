package hr.foi.air.otpstudent.domain.model

data class Reward(
    val id: String,
    val title: String,
    val description: String,
    val costPoints: Long,
    val active: Boolean,
    val validDays: Long,
    val channel: String,
    val barcodeFormat: String,
    val imageUrl: String? = null,
    val maxPerUser: Long = 0L,

    // filter
    val category: RewardsFilter = RewardsFilter.OPT_REWARDS
)
