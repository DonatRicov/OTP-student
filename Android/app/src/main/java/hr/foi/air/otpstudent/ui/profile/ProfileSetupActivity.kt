package hr.foi.air.otpstudent.ui.profile

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import hr.foi.air.otpstudent.R
import java.io.File
import java.util.Calendar

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var currentUid: String

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
    private lateinit var acFaculty: AutoCompleteTextView
    private lateinit var acMajor: AutoCompleteTextView
    private lateinit var acEducationLevel: AutoCompleteTextView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDiscardChangesDialog()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val user = auth.currentUser ?: return
        val uid = user.uid
        currentUid = uid

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            showDiscardChangesDialog()
        }

        imgAvatar = findViewById(R.id.imgAvatar)
        imgEditPhoto = findViewById(R.id.imgEditPhoto)

        val avatarClickListener = View.OnClickListener {
            showChooseImageDialog()
        }
        imgAvatar.setOnClickListener(avatarClickListener)
        imgEditPhoto.setOnClickListener(avatarClickListener)

        tvFullNameHeader = findViewById(R.id.tvFullName)

        etFirstName       = findViewById(R.id.tvNameValue)
        etLastName        = findViewById(R.id.etLastName)
        etEmail           = findViewById(R.id.tvEmailValue)
        etPassword        = findViewById(R.id.tvPasswordValue)
        etPhone           = findViewById(R.id.tvPhoneValue)
        etLocation        = findViewById(R.id.tvLocationValue)
        etBirthday        = findViewById(R.id.tvBirthdayValue)

        acGender          = findViewById(R.id.tvGenderValue)
        acFaculty         = findViewById(R.id.tvFacultyValue)
        acMajor           = findViewById(R.id.tvMajorValue)
        acEducationLevel  = findViewById(R.id.tvEducationLevelValue)

        btnSave           = findViewById(R.id.btnSave)
        btnCancel         = findViewById(R.id.btnCancel)

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {

                    val fullName = doc.getString("fullName") ?: ""
                    if (fullName.isNotEmpty()) {
                        val parts = fullName.trim().split(" ")
                        val first = parts.firstOrNull() ?: ""
                        val last  = parts.drop(1).joinToString(" ")

                        etFirstName.setText(first)
                        etLastName.setText(last)
                        tvFullNameHeader.text = fullName
                    }

                    doc.getString("firstName")?.let { if (it.isNotEmpty()) etFirstName.setText(it) }
                    doc.getString("lastName")?.let  { if (it.isNotEmpty()) etLastName.setText(it) }

                    etEmail.setText(
                        doc.getString("email") ?: user.email ?: ""
                    )
                    etPhone.setText(
                        doc.getString("phone") ?: ""
                    )
                    etLocation.setText(
                        doc.getString("location") ?: ""
                    )
                    etBirthday.setText(
                        doc.getString("birthday") ?: ""
                    )

                    acFaculty.setText(
                        doc.getString("faculty")
                            ?: "Fakultet organizacije i informatike",
                        false
                    )
                    acMajor.setText(
                        doc.getString("major")
                            ?: "Informacijsko i programsko inženjerstvo",
                        false
                    )
                    acEducationLevel.setText(
                        doc.getString("educationLevel") ?: "",
                        false
                    )
                    acGender.setText(
                        doc.getString("gender") ?: "—",
                        false
                    )

                    doc.getString("avatarUrl")
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { url ->
                            Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(imgAvatar)
                        }

                    etPassword.setText("************")
                } else {
                    etEmail.setText(user.email ?: "")
                    tvFullNameHeader.text = "Uredi profil"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Greška pri dohvaćanju podataka: ${e.message}", Toast.LENGTH_LONG).show()
            }

        etBirthday.setOnClickListener {
            showDatePicker { formatted ->
                etBirthday.setText(formatted)
            }
        }


        btnSave.setOnClickListener {
            val firstName      = etFirstName.text?.toString()?.trim() ?: ""
            val lastName       = etLastName.text?.toString()?.trim() ?: ""
            val fullName       = listOf(firstName, lastName)
                .filter { it.isNotEmpty() }
                .joinToString(" ")

            val email          = etEmail.text?.toString()?.trim() ?: ""
            val phone          = etPhone.text?.toString()?.trim() ?: ""
            val location       = etLocation.text?.toString()?.trim() ?: ""
            val birthday       = etBirthday.text?.toString()?.trim() ?: ""
            val gender         = acGender.text?.toString()?.trim() ?: ""
            val faculty        = acFaculty.text?.toString()?.trim() ?: ""
            val major          = acMajor.text?.toString()?.trim() ?: ""
            val educationLevel = acEducationLevel.text?.toString()?.trim() ?: ""

            val data = mapOf(
                "fullName"       to fullName,
                "firstName"      to firstName,
                "lastName"       to lastName,
                "email"          to email,
                "phone"          to phone,
                "location"       to location,
                "birthday"       to birthday,
                "gender"         to gender,
                "faculty"        to faculty,
                "major"          to major,
                "educationLevel" to educationLevel
            )

            db.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Podaci spremljeni", Toast.LENGTH_SHORT).show()
                    if (fullName.isNotEmpty()) {
                        tvFullNameHeader.text = fullName
                    }
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Greška pri spremanju: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        btnCancel.setOnClickListener {
            showDiscardChangesDialog()
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

        uploadAvatarToStorage(currentUid, uri)
    }

    private fun uploadAvatarToStorage(uid: String, imageUri: Uri) {


        val ref = storage.reference.child("avatars/$uid/profile.jpg")

        ref.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Upload nije uspio")
                }
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                val url = downloadUri.toString()
                db.collection("users").document(uid)
                    .set(mapOf("avatarUrl" to url), SetOptions.merge())
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Greška pri uploadu slike: ${e.message}", Toast.LENGTH_LONG).show()
            }
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

        val btnDiscard = dialogView.findViewById<MaterialButton>(
            R.id.btnDiscardChanges
        )
        val btnContinue = dialogView.findViewById<MaterialButton>(
            R.id.btnContinueEditing
        )

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