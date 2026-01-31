package hr.foi.air.otpstudent

import android.app.Application
import hr.foi.air.otpstudent.data.push.NotificationChannels
import hr.foi.air.otpstudent.di.AppModule

class OtpStudentApp : Application() {
    override fun onCreate() {
        super.onCreate()

        AppModule.init(this)

        NotificationChannels.ensureCreated(this)
    }
}
