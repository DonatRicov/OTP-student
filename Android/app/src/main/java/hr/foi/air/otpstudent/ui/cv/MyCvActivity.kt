package hr.foi.air.otpstudent.ui.cv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import hr.foi.air.otpstudent.domain.model.CvDocument
import android.widget.TextView

class MyCvActivity : AppCompatActivity() {

    private lateinit var adapter: CvAdapter
    private lateinit var auth: FirebaseAuth

    private val viewModel: CvViewModel by lazy {
        ViewModelProvider(this, CvVmFactory())[CvViewModel::class.java]
    }

    private val pdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadCv(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cv)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Molimo prijavite se.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()

        findViewById<Button>(R.id.btnUploadPdf).setOnClickListener {
            pdfLauncher.launch("application/pdf")
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                render(state)
            }
        }

        lifecycleScope.launch {
            viewModel.effects.collectLatest { eff ->
                when (eff) {
                    is CvEffect.ShowMessage ->
                        Toast.makeText(this@MyCvActivity, eff.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.load()
    }

    private fun setupRecyclerView() {
        adapter = CvAdapter(
            emptyList(),
            onItemClick = { cv -> openPdf(cv) },
            onDeleteClick = { cv -> viewModel.deleteCv(cv) }
        )

        val rv = findViewById<RecyclerView>(R.id.recyclerViewCvs)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun render(state: CvUiState) {
        state.error?.let {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

        findViewById<TextView>(R.id.tvUserName).text =
            state.fullName.ifBlank { "Korisnik" }

        findViewById<TextView>(R.id.tvUserEmail).text =
            state.email.ifBlank { "—" }

        // ovo je value desno; label lijevo ćemo promijeniti u "Smjer"
        findViewById<TextView>(R.id.tvUserPosition).text =
            state.major.ifBlank { "—" }

        findViewById<TextView>(R.id.tvUserLocation).text =
            state.location.ifBlank { "—" }

        val avatar = findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivUserAvatar)
        if (state.avatarUrl.isNotBlank()) {
            com.bumptech.glide.Glide.with(this)
                .load(state.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(avatar)
        } else {
            avatar.setImageResource(R.drawable.ic_profile_placeholder)
        }

        adapter.updateData(state.cvs)
        findViewById<Button>(R.id.btnUploadPdf).isEnabled = !state.isUploading
    }


    private fun openPdf(cv: CvDocument) {
        val uri = Uri.parse(cv.fileUrl)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Nemate aplikaciju za PDF (pokušajte kroz browser).", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private inner class CvVmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CvViewModel::class.java)) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw IllegalStateException("User not logged in")

                val repo = AppModule.provideCvRepository(uid)
                val authRepo = AppModule.authRepository
                val userIdProvider = { FirebaseAuth.getInstance().currentUser?.uid }

                @Suppress("UNCHECKED_CAST")
                return CvViewModel(repo, authRepo, userIdProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
