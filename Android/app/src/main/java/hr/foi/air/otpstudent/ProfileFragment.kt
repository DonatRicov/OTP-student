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
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import android.content.Context
import com.bumptech.glide.Glide

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth

    private var cameraImageUri: Uri? = null

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
        val user = auth.currentUser

        val tvName     = view.findViewById<TextView>(R.id.tvProfileName)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvProfileSubtitle)
        val imgAvatar  = view.findViewById<ImageView>(R.id.imgAvatar)
        val imgEdit    = view.findViewById<ImageView>(R.id.imgEditPhoto)

        val prefs = requireContext()
            .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

        val savedAvatarUri = prefs.getString("avatar_uri", null)
        if (savedAvatarUri != null) {
            Glide.with(this)
                .load(Uri.parse(savedAvatarUri))
                .circleCrop()
                .into(imgAvatar)
        }




        val nameFromEmail = user?.email
            ?.substringBefore("@")
            ?.replaceFirstChar { it.uppercase() }
            ?: "Korisnik"

        tvName.text = nameFromEmail
        tvSubtitle.text = "FOI student"


        val avatarClickListener = View.OnClickListener {
            showChooseImageDialog()
        }
        imgAvatar.setOnClickListener(avatarClickListener)
        imgEdit.setOnClickListener(avatarClickListener)

        // Odjava
        view.findViewById<LinearLayout>(R.id.rowLogout).setOnClickListener {
            auth.signOut()

            val ctx = requireActivity()
            val i = Intent(ctx, StartActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(i)
        }

        view.findViewById<LinearLayout>(R.id.rowCv).setOnClickListener {
            // TODO: otvori ekran za životopis
        }

        view.findViewById<LinearLayout>(R.id.rowPractice).setOnClickListener {
            // TODO: odrađene prakse
        }

        view.findViewById<LinearLayout>(R.id.rowJobs).setOnClickListener {
            // TODO: moji poslovi
        }

        view.findViewById<LinearLayout>(R.id.rowProfileSettings).setOnClickListener {
            // TODO: postavke profila
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
        view?.findViewById<ImageView>(R.id.imgAvatar)?.let { imageView ->
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .into(imageView)
        }

        val prefs = requireContext()
            .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

        prefs.edit()
            .putString("avatar_uri", uri.toString())
            .apply()
    }

}
