package hr.foi.air.otpstudent.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hr.foi.air.otpstudent.MainActivity
import hr.foi.air.otpstudent.QuickLoginOfferActivity
import hr.foi.air.otpstudent.R

class LoginSuccessActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_success)

        val img = findViewById<ImageView>(R.id.imgSuccess)
        val tv  = findViewById<TextView>(R.id.tvSuccess)

        img.scaleX = 0f
        img.scaleY = 0f
        img.alpha = 0f
        tv.alpha = 0f

        val scaleX = ObjectAnimator.ofFloat(img, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(img, View.SCALE_Y, 0f, 1f)
        val alpha  = ObjectAnimator.ofFloat(img, View.ALPHA, 0f, 1f)

        scaleX.duration = 500
        scaleY.duration = 500
        alpha.duration = 500

        val set = AnimatorSet()
        set.playTogether(scaleX, scaleY, alpha)
        set.interpolator = OvershootInterpolator()
        set.start()

        tv.animate()
            .alpha(1f)
            .setStartDelay(400)
            .setDuration(400)
            .start()

        handler.postDelayed({
            val prefs = getSharedPreferences("otp_prefs", MODE_PRIVATE)
            prefs.edit().remove("quick_login_offer_shown").apply()
            val shown = prefs.getBoolean("quick_login_offer_shown", false)


            val next = if (!shown) {
                prefs.edit().putBoolean("quick_login_offer_shown", true).apply()
                Intent(this, QuickLoginOfferActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }

            startActivity(next)
            finish()
        }, 2000)

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

}