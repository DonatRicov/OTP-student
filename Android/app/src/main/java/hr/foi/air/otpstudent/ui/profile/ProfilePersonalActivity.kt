package hr.foi.air.otpstudent.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import hr.foi.air.otpstudent.MainActivity
import hr.foi.air.otpstudent.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import hr.foi.air.otpstudent.di.AppModule

class ProfilePersonalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_personal)

        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName  = findViewById<TextInputEditText>(R.id.etLastName)
        val etGender = findViewById<MaterialAutoCompleteTextView>(R.id.etGender)

        val genderOptions = listOf("Muško", "Žensko", "Drugo")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, genderOptions)
        etGender.setAdapter(adapter)

        val btnNext  = findViewById<MaterialButton>(R.id.btnNextPersonal)
        val tvSkip   = findViewById<TextView>(R.id.tvSkipPersonal)

        tvSkip.setOnClickListener {
            goToMain()
        }

        btnNext.setOnClickListener {
            val firstName = etFirstName.text?.toString()?.trim().orEmpty()
            val lastName  = etLastName.text?.toString()?.trim().orEmpty()
            val gender    = etGender.text?.toString()?.trim().orEmpty()

            val uid = AppModule.authRepository.currentUserId()
            if (uid != null) {
                lifecycleScope.launch {
                    AppModule.authRepository.updateUserFields(
                        uid,
                        mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "gender" to gender
                        )
                    )
                    startActivity(Intent(this@ProfilePersonalActivity, ProfileFacultyActivity::class.java))
                }
            } else {
                startActivity(Intent(this, ProfileFacultyActivity::class.java))
            }
        }

    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }
}