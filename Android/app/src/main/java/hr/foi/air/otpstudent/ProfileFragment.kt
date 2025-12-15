package hr.foi.air.otpstudent

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var currentUid: String

    private var cameraImageUri: Uri? = null

    // referencirani view-ovi
    private lateinit var tvName: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var imgAvatar: ImageView
    private lateinit var imgEdit: ImageView

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val user = auth.currentUser
        if (user == null) {
            // mozemo tu satavit da ga bacimo na login but ovako je ok za sad
            return
        }
        currentUid = user.uid

        tvName     = view.findViewById(R.id.tvProfileName)
        tvSubtitle = view.findViewById(R.id.tvProfileSubtitle)
        imgAvatar  = view.findViewById(R.id.imgAvatar)
        imgEdit    = view.findViewById(R.id.imgEditPhoto)

        val avatarClickListener = View.OnClickListener {
            showChooseImageDialog()
        }
        imgAvatar.setOnClickListener(avatarClickListener)
        imgEdit.setOnClickListener(avatarClickListener)

        // učitaj podatke
        loadProfileData()

        // Odjava
        view.findViewById<LinearLayout>(R.id.rowLogout).setOnClickListener {
            auth.signOut()

            val ctx = requireActivity()
            val i = Intent(ctx, StartActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(i)
            ctx.finish() //
        }


        view.findViewById<LinearLayout>(R.id.rowCv).setOnClickListener {
            val intent = Intent(requireContext(), MyCvActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<LinearLayout>(R.id.rowPractice).setOnClickListener {
            // TO DO: Odradener prakse
        }

        view.findViewById<LinearLayout>(R.id.rowJobs).setOnClickListener {
            // TO DO: Moji poslovi
        }

        view.findViewById<LinearLayout>(R.id.rowProfileSettings).setOnClickListener {
            val intent = Intent(requireContext(), ProfileSetupActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        if(this::currentUid.isInitialized) {
            loadProfileData()
        }
    }

    private fun loadProfileData() {
        val user = auth.currentUser
        if (user == null) return

        db.collection("users").document(currentUid).get()
            .addOnSuccessListener { doc ->
                val nameFromEmail = user.email
                    ?.substringBefore("@")
                    ?.replaceFirstChar { it.uppercase() }
                    ?: "Korisnik"

                val fullName = doc.getString("fullName")

                tvName.text = when {
                    !fullName.isNullOrBlank() -> fullName
                    else -> nameFromEmail
                }

                // ovo promjeniti ovisi o tome sto kaze Frane
                tvSubtitle.text = "FOI student"

                // profilna iz baze ako ju ima
                doc.getString("avatarUrl")
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { url ->
                        Glide.with(this)
                            .load(url)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(imgAvatar)
                    }
            }
            .addOnFailureListener {
                val user = auth.currentUser
                val nameFromEmail = user?.email
                    ?.substringBefore("@")
                    ?.replaceFirstChar { it.uppercase() }
                    ?: "Korisnik"

                tvName.text = nameFromEmail
                tvSubtitle.text = "FOI student"
            }
    }

    private fun showChooseImageDialog() {
        AlertDialog.Builder(requireContext())
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
        val context = requireContext()
        val imageFile = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        takePictureLauncher.launch(cameraImageUri)
    }

    private fun setAvatarImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
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
            .addOnFailureListener { _ ->
                // fallaback mozda toast dodamo kansije
            }
    }
}
