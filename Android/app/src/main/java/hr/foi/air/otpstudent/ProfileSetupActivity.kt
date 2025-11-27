package hr.foi.air.otpstudent

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.widget.Toast

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvEmailValue: TextView
    private lateinit var tvPasswordValue: TextView
    private lateinit var tvPhoneValue: TextView
    private lateinit var tvNameValue: TextView
    private lateinit var tvLocationValue: TextView
    private lateinit var tvBirthdayValue: TextView
    private lateinit var tvFacultyValue: TextView
    private lateinit var tvMajorValue: TextView
    private lateinit var tvEducationLevelValue: TextView
    private lateinit var tvGenderValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        // gumbic za back
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser ?: return
        val uid = user.uid

        tvEmailValue          = findViewById(R.id.tvEmailValue)
        tvPasswordValue       = findViewById(R.id.tvPasswordValue)
        tvPhoneValue          = findViewById(R.id.tvPhoneValue)
        tvNameValue           = findViewById(R.id.tvNameValue)
        tvLocationValue       = findViewById(R.id.tvLocationValue)
        tvBirthdayValue       = findViewById(R.id.tvBirthdayValue)
        tvFacultyValue        = findViewById(R.id.tvFacultyValue)
        tvMajorValue          = findViewById(R.id.tvMajorValue)
        tvEducationLevelValue = findViewById(R.id.tvEducationLevelValue)
        tvGenderValue         = findViewById(R.id.tvGenderValue)

        val rowEmail           = findViewById<LinearLayout>(R.id.rowEmail)
        val rowPassword        = findViewById<LinearLayout>(R.id.rowPassword)
        val rowPhone           = findViewById<LinearLayout>(R.id.rowPhone)
        val rowName            = findViewById<LinearLayout>(R.id.rowName)
        val rowGender          = findViewById<LinearLayout>(R.id.rowGender)
        val rowLocation        = findViewById<LinearLayout>(R.id.rowLocation)
        val rowBirthday        = findViewById<LinearLayout>(R.id.rowBirthday)
        val rowFaculty         = findViewById<LinearLayout>(R.id.rowFaculty)
        val rowMajor           = findViewById<LinearLayout>(R.id.rowMajor)
        val rowEducationLevel  = findViewById<LinearLayout>(R.id.rowEducationLevel)


        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                tvEmailValue.text          = doc.getString("email") ?: user.email ?: ""
                tvPhoneValue.text          = doc.getString("phone") ?: "dodajte broj telefona"
                tvNameValue.text           = doc.getString("fullName") ?: "Ime i prezime"
                tvLocationValue.text       = doc.getString("location") ?: "Lokacija"
                tvBirthdayValue.text       = doc.getString("birthday") ?: "dodajte datum rođenja"
                tvFacultyValue.text        = doc.getString("faculty") ?: "Fakultet organizacije i informatike"
                tvMajorValue.text          = doc.getString("major") ?: "Informacijsko i programsko inženjerstvo"
                tvEducationLevelValue.text = doc.getString("educationLevel") ?: "dodajte razinu obrazovanja"
                tvGenderValue.text         = doc.getString("gender") ?: "—"
                // Lozinku moram reworkat
                tvPasswordValue.text       = "************"
            }


        // Email
        rowEmail.setOnClickListener {
            showEditDialog(
                title = "Uredite email",
                currentValue = tvEmailValue.text.toString()
            ) { newValue ->
                tvEmailValue.text = newValue
                saveField(uid, "email", newValue)

            }
        }

        // Lozinka
        rowPassword.setOnClickListener {
            showEditDialog(
                title = "Uredite lozinku",
                currentValue = ""
            ) { newPassword ->
                tvPasswordValue.text = "************"
                saveField(uid, "passwordHint", "set")

            }
        }

        // Telefon
        rowPhone.setOnClickListener {
            showEditDialog(
                title = "Uredite broj telefona",
                currentValue = tvPhoneValue.text.toString()
            ) { newValue ->
                tvPhoneValue.text = newValue
                saveField(uid, "phone", newValue)
            }
        }

        // Ime i prezime
        rowName.setOnClickListener {
            showEditDialog(
                title = "Uredite ime i prezime",
                currentValue = tvNameValue.text.toString()
            ) { newValue ->
                tvNameValue.text = newValue
                saveField(uid, "fullName", newValue)
            }
        }

        // Spol
        rowGender.setOnClickListener {
            showEditDialog(
                title = "Uredite spol",
                currentValue = tvGenderValue.text.toString()
            ) { newValue ->
                tvGenderValue.text = newValue
                saveField(uid, "gender", newValue)
            }
        }

        // Lokacija
        rowLocation.setOnClickListener {
            showEditDialog(
                title = "Uredite lokaciju",
                currentValue = tvLocationValue.text.toString()
            ) { newValue ->
                tvLocationValue.text = newValue
                saveField(uid, "location", newValue)
            }
        }

        // Datum rođenja
        rowBirthday.setOnClickListener {
            showEditDialog(
                title = "Uredite datum rođenja",
                currentValue = tvBirthdayValue.text.toString()
            ) { newValue ->
                tvBirthdayValue.text = newValue
                saveField(uid, "birthday", newValue)
            }
        }

        // Fakultet
        rowFaculty.setOnClickListener {
            showEditDialog(
                title = "Uredite fakultet",
                currentValue = tvFacultyValue.text.toString()
            ) { newValue ->
                tvFacultyValue.text = newValue
                saveField(uid, "faculty", newValue)
            }
        }

        // Smjer
        rowMajor.setOnClickListener {
            showEditDialog(
                title = "Uredite smjer",
                currentValue = tvMajorValue.text.toString()
            ) { newValue ->
                tvMajorValue.text = newValue
                saveField(uid, "major", newValue)
            }
        }

        // Razina obrazovanja
        rowEducationLevel.setOnClickListener {
            showEditDialog(
                title = "Uredite razinu obrazovanja",
                currentValue = tvEducationLevelValue.text.toString()
            ) { newValue ->
                tvEducationLevelValue.text = newValue
                saveField(uid, "educationLevel", newValue)
            }
        }
    }

    private fun showEditDialog(
        title: String,
        currentValue: String,
        onConfirm: (String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_edit_field, null)

        val etValue = dialogView.findViewById<EditText>(R.id.etDialogValue)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)

        tvTitle.text = title
        etValue.setText(currentValue)
        etValue.setSelection(etValue.text.length)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Spremi") { _, _ ->
                val newValue = etValue.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    onConfirm(newValue)
                }
            }
            .setNegativeButton("Odustani") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveField(uid: String, fieldName: String, value: String) {
        val data = mapOf(fieldName to value)

        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Spremljeno", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Greška pri spremanju: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

}
