package hr.foi.air.otpstudent.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import hr.foi.air.otpstudent.MainActivity
import hr.foi.air.otpstudent.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import hr.foi.air.otpstudent.di.AppModule

class ProfileFacultyActivity : AppCompatActivity() {

    private val facultyFixed = "Fakultet organizacije i informatike"

    private val levels = listOf(
        "sveučilišni prijediplomski",
        "sveučilišni displomski",
        "stručni prijediplomski",
        "sveučilišni specijalistički",
        "doktorski",
        "cjeloživotno učenje"
    )

    private val majorsByLevel: Map<String, List<String>> = mapOf(
        "sveučilišni prijediplomski" to listOf(
            "Informacijski i poslovni sustavi 1.2 (IPS)",
            "Ekonomika poduzetništva 1.2 (EP)"
        ),
        "sveučilišni displomski" to listOf(
            "Baze podataka i baze znanja 1.4 (BPBZ)",
            "Informacijsko i programsko inžinjerstvo 1.4 (IPI)",
            "Informatika u obrazovanju 1.4 (IUO)",
            "Organizacija poslovnih sustava 1.4. (OPS)",
            "Ekonomika poduzetništva 1.1 (EP-DS)"
        ),
        "stručni prijediplomski" to listOf(
            "Informacijske tehnologije i digitalizacija poslovanja 1.3 (ITDP)"
        ),
        "sveučilišni specijalistički" to listOf(
            "Menadžment poslovnih sustava 1.0 (PDSSMPS)",
            "Upravljanje sigurnošću i revizija informacijskih sustava 2.0 (PDSSSRIS)",
            "E-učenje u obrazovanju i poslovanju 1.0 (PDEU)"
        ),
        "doktorski" to listOf(
            "Doktorski studij Informacijskih znanosti 1.1 (PDDSIZ)",
            "Upravljanje digitalnim inovacijama 1.0 (UDI-DOK)"
        ),
        "cjeloživotno učenje" to listOf(
            "Pedagoško-psihološko-didaktičko-metodičko obrazovanje 1.0 (PPDMO)"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_faculty)

        val etFaculty = findViewById<TextInputEditText>(R.id.etFaculty)
        val actvLevel = findViewById<android.widget.AutoCompleteTextView>(R.id.actvLevel)
        val actvMajor = findViewById<android.widget.AutoCompleteTextView>(R.id.actvMajor)
        val etYear    = findViewById<TextInputEditText>(R.id.etYear)

        val btnSave = findViewById<MaterialButton>(R.id.btnSaveFaculty)
        val tvSkip  = findViewById<TextView>(R.id.tvSkipFaculty)

        etFaculty.setText(facultyFixed)
        etFaculty.isEnabled = false

        val levelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, levels)
        actvLevel.setAdapter(levelAdapter)

        actvMajor.isEnabled = false

        actvLevel.setOnItemClickListener { _, _, position, _ ->
            val selectedLevel = levels[position]

            actvMajor.setText("", false)

            val majors = majorsByLevel[selectedLevel].orEmpty()
            val majorAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, majors)
            actvMajor.setAdapter(majorAdapter)

            actvMajor.isEnabled = majors.isNotEmpty()
        }

        tvSkip.setOnClickListener { goToMain() }

        btnSave.setOnClickListener {
            val faculty = etFaculty.text?.toString()?.trim().orEmpty()
            val level   = actvLevel.text?.toString()?.trim().orEmpty()
            val major   = actvMajor.text?.toString()?.trim().orEmpty()
            val yearStr = etYear.text?.toString()?.trim().orEmpty()
            val year    = yearStr.toIntOrNull()

            val uid = AppModule.authRepository.currentUserId()
            if (uid != null) {
                lifecycleScope.launch {
                    AppModule.authRepository.updateUserFields(
                        uid,
                        mapOf(
                            "faculty" to faculty,
                            "educationLevel" to level,
                            "major" to major,
                            "studyYear" to year
                        )
                    )
                    goToMain()
                }
            } else {
                goToMain()
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
