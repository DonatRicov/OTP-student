package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity

class ProfileFacultyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_faculty)

        val etFaculty = findViewById<TextInputEditText>(R.id.etFaculty)
        val etMajor   = findViewById<TextInputEditText>(R.id.etMajor)
        val etLevel   = findViewById<TextInputEditText>(R.id.etLevel)
        val etYear    = findViewById<TextInputEditText>(R.id.etYear)

        val btnSave = findViewById<MaterialButton>(R.id.btnSaveFaculty)
        val tvSkip  = findViewById<TextView>(R.id.tvSkipFaculty)

        tvSkip.setOnClickListener {
            goToMain()
        }

        btnSave.setOnClickListener {
            // za firestore dio
            goToMain()
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }
}
