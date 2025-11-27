package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val btnRegister = findViewById<MaterialButton>(R.id.btnStartRegister)
        val tvLogin     = findViewById<TextView>(R.id.tvStartLogin)
        val tvTagline   = findViewById<TextView>(R.id.tvTagline)

        val html = """
    <font color="#005F3A">OTP</font><font color="#F7941D">akiraj</font> svoju karijeru!
""".trimIndent()

        tvTagline.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)


        tvTagline.text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
