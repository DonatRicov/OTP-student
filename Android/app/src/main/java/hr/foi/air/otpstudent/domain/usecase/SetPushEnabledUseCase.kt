package hr.foi.air.otpstudent.domain.usecase

import hr.foi.air.otpstudent.domain.repository.PushRepository

class SetPushEnabledUseCase(
    private val pushRepository: PushRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        pushRepository.setEnabled(enabled)
    }
}
