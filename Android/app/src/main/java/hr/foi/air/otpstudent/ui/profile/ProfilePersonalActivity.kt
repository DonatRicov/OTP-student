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
            val intent = Intent(this, ProfileFacultyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }
}