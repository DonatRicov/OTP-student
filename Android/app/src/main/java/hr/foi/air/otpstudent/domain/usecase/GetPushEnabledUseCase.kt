package hr.foi.air.otpstudent.domain.usecase

import hr.foi.air.otpstudent.domain.repository.PushRepository

class GetPushEnabledUseCase(
    private val pushRepository: PushRepository
) {
    suspend operator fun invoke(): Boolean = pushRepository.isEnabled()
}