package hr.foi.air.otpstudent.ui.profile

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import java.io.File
import java.util.Calendar
import kotlinx.coroutines.flow.collectLatest

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel

    private lateinit var imgAvatar: ShapeableImageView
    private lateinit var imgEditPhoto: ImageView
    private var cameraImageUri: Uri? = null

    private lateinit var tvFullNameHeader: TextView

    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etLocation: TextInputEditText
    private lateinit var etBirthday: TextInputEditText

    private lateinit var acGender: AutoCompleteTextView

    private lateinit var etFaculty: TextInputEditText
    private lateinit var actvLevel: AutoCompleteTextView
    private lateinit var actvMajor: AutoCompleteTextView
    private lateinit var etStudyYear: TextInputEditText

    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { setAvatarImage(it) }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                cameraImageUri?.let { setAvatarImage(it) }
            }
        }

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
        setContentView(R.layout.activity_profile_setup)

        viewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(AppModule.authRepository)
        )[ProfileViewModel::class.java]

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDiscardChangesDialog()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            showDiscardChangesDialog()
        }

        imgAvatar = findViewById(R.id.imgAvatar)
        imgEditPhoto = findViewById(R.id.imgEditPhoto)

        val avatarClickListener = View.OnClickListener { showChooseImageDialog() }
        imgAvatar.setOnClickListener(avatarClickListener)
        imgEditPhoto.setOnClickListener(avatarClickListener)

        tvFullNameHeader = findViewById(R.id.tvFullName)

        etFirstName = findViewById(R.id.tvNameValue)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.tvEmailValue)
        etPassword = findViewById(R.id.tvPasswordValue)
        etPhone = findViewById(R.id.tvPhoneValue)
        etLocation = findViewById(R.id.tvLocationValue)
        etBirthday = findViewById(R.id.tvBirthdayValue)
        acGender = findViewById(R.id.tvGenderValue)

        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        etPassword.apply {
            setText("************")
            isEnabled = false
            isFocusable = false
            isFocusableInTouchMode = false
            keyListener = null
        }

        etPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        val genders = listOf("Muško", "Žensko", "Ostalo")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, genders)
        acGender.setAdapter(genderAdapter)
        acGender.keyListener = null

        etFaculty = findViewById(R.id.etFaculty)
        actvLevel = findViewById(R.id.actvEducationLevel)
        actvMajor = findViewById(R.id.actvMajor)
        etStudyYear = findViewById(R.id.etStudyYear)

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

        etBirthday.setOnClickListener {
            showDatePicker { formatted -> etBirthday.setText(formatted) }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                when (state) {
                    ProfileState.Idle -> Unit

                    ProfileState.Loading -> {
                        btnSave.isEnabled = false
                    }

                    is ProfileState.Loaded -> {
                        btnSave.isEnabled = true
                        bindProfileToUi(state.ui)
                    }

                    is ProfileState.Saved -> {
                        Toast.makeText(this@ProfileSetupActivity, state.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }

                    is ProfileState.Error -> {
                        btnSave.isEnabled = true
                        Toast.makeText(this@ProfileSetupActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewModel.load()

        btnSave.setOnClickListener {
            val firstName = etFirstName.text?.toString()?.trim().orEmpty()
            val lastName = etLastName.text?.toString()?.trim().orEmpty()
            val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")

            val email = etEmail.text?.toString()?.trim().orEmpty()
            val phone = etPhone.text?.toString()?.trim().orEmpty()
            val location = etLocation.text?.toString()?.trim().orEmpty()
            val birthday = etBirthday.text?.toString()?.trim().orEmpty()
            val gender = acGender.text?.toString()?.trim().orEmpty()

            val faculty = etFaculty.text?.toString()?.trim().orEmpty().ifEmpty { facultyFixed }
            val educationLevel = actvLevel.text?.toString()?.trim().orEmpty()
            val major = actvMajor.text?.toString()?.trim().orEmpty()
            val studyYear = etStudyYear.text?.toString()?.trim()?.toIntOrNull()

            val fields = mutableMapOf<String, Any?>(
                "fullName" to fullName,
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "phone" to phone,
                "location" to location,
                "birthday" to birthday,
                "gender" to gender,
                "faculty" to faculty,
                "educationLevel" to educationLevel,
                "major" to major,
                "studyYear" to studyYear
            ).filterValues { it != null }

            viewModel.save(fields)
        }

        btnCancel.setOnClickListener {
            showDiscardChangesDialog()
        }
    }

    private fun bindProfileToUi(ui: ProfileUi) {
        val fullName = ui.fullName.ifBlank {
            listOf(ui.firstName, ui.lastName).filter { it.isNotBlank() }.joinToString(" ")
        }

        tvFullNameHeader.text = fullName.ifBlank { "Uredi profil" }

        etFirstName.setText(ui.firstName)
        etLastName.setText(ui.lastName)

        etEmail.setText(ui.email)
        etPhone.setText(ui.phone)
        etLocation.setText(ui.location)
        etBirthday.setText(ui.birthday)

        acGender.setText(ui.gender.ifBlank { "—" }, false)

        etFaculty.setText(if (ui.faculty.isBlank()) facultyFixed else ui.faculty)

        if (ui.educationLevel.isNotBlank()) {
            actvLevel.setText(ui.educationLevel, false)
            val majors = majorsByLevel[ui.educationLevel].orEmpty()
            actvMajor.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, majors))
            actvMajor.isEnabled = majors.isNotEmpty()
        }

        if (ui.major.isNotBlank()) {
            actvMajor.setText(ui.major, false)
        }

        etStudyYear.setText(ui.studyYear)

        if (ui.avatarUrl.isNotBlank()) {
            Glide.with(this)
                .load(ui.avatarUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(imgAvatar)
        }
    }

    private fun showChooseImageDialog() {
        AlertDialog.Builder(this)
            .setTitle("Profilna slika")
            .setItems(arrayOf("Odaberi iz galerije", "Slikaj kamerom")) { _, which ->
                when (which) {
                    0 -> pickFromGallery()
                    1 -> takePhoto()
                }
            }
            .show()
    }

    private fun pickFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun takePhoto() {
        val imageFile = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imageFile
        )
        takePictureLauncher.launch(cameraImageUri!!)
    }

    private fun setAvatarImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(imgAvatar)

        viewModel.uploadAvatar(uri)
    }

    private fun showDatePicker(onDatePicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, y, m, d ->
            val dayStr = d.toString().padStart(2, '0')
            val monthStr = (m + 1).toString().padStart(2, '0')
            onDatePicked("$dayStr.$monthStr.$y.")
        }, year, month, day).show()
    }

    private fun showDiscardChangesDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_discard_changes, null)

        val btnDiscard = dialogView.findViewById<MaterialButton>(R.id.btnDiscardChanges)
        val btnContinue = dialogView.findViewById<MaterialButton>(R.id.btnContinueEditing)

        val alertDialog = MaterialAlertDialogBuilder(this, R.style.RoundedDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnDiscard.setOnClickListener {
            alertDialog.dismiss()
            finish()
        }

        btnContinue.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}
