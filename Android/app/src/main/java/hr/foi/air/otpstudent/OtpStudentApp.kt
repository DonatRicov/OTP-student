package hr.foi.air.otpstudent

import android.app.Application
import hr.foi.air.otpstudent.data.push.NotificationChannels
import hr.foi.air.otpstudent.di.AppModule
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import hr.foi.air.otpstudent.data.auth.AppLockStore
import android.content.ComponentCallbacks2

class OtpStudentApp : Application() {
    override fun onCreate() {
        super.onCreate()

        AppModule.init(this)
        NotificationChannels.ensureCreated(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                AppLockStore.markBackgrounded(this@OtpStudentApp)
            }
        })
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            AppLockStore.markBackgrounded(this)
        }
    }

}