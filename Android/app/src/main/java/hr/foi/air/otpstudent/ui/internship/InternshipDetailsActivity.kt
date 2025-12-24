package hr.foi.air.otpstudent.ui.internship

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.auth.FirebaseAuth
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import android.content.Context

class InternshipDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "internship_id"

        fun newIntent(context: Context, internshipId: String): Intent {
            return Intent(context, InternshipDetailsActivity::class.java).apply {
                putExtra(EXTRA_ID, internshipId)
            }
        }
    }


    private val viewModel: InternshipDetailsViewModel by lazy {
        ViewModelProvider(this, VmFactory())[InternshipDetailsViewModel::class.java]
    }

    private lateinit var ivFavorite: ImageView
    private lateinit var btnApply: MaterialButton
    private lateinit var cbApplied: MaterialCheckBox

    private lateinit var tvTitle: TextView
    private lateinit var tvCompany: TextView
    private lateinit var tvComp: TextView
    private lateinit var tvLoc: TextView
    private lateinit var tvDur: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvAreas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internship_details)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        ivFavorite = findViewById(R.id.ivFavorite)
        btnApply = findViewById(R.id.btnApply)
        cbApplied = findViewById(R.id.cbAppliedDetails)

        tvTitle = findViewById(R.id.tvInternshipTitle)
        tvCompany = findViewById(R.id.tvCompanyName)
        tvComp = findViewById(R.id.tvCompensationValue)
        tvLoc = findViewById(R.id.tvLocationValue)
        tvDur = findViewById(R.id.tvDurationValue)
        tvDesc = findViewById(R.id.tvDescription)
        tvAreas = findViewById(R.id.tvAreas)

        val internshipId = intent.getStringExtra(EXTRA_ID)
        if (internshipId.isNullOrBlank()) {
            Toast.makeText(this, "Greška: ID internship-a nije pronađen.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        ivFavorite.setOnClickListener { viewModel.toggleFavorite() }

        // checkbox je samo display
        cbApplied.setOnClickListener {
            cbApplied.isChecked = viewModel.state.value.isApplied
            Toast.makeText(
                this,
                if (viewModel.state.value.isApplied) "Internship je prijavljen." else "Internship nije prijavljen.",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnApply.setOnClickListener {
            val url = viewModel.state.value.internship?.applyUrl.orEmpty()
            if (url.isBlank()) {
                Toast.makeText(this, "Link za prijavu nije dostupan.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loggedIn = FirebaseAuth.getInstance().currentUser != null
            if (!loggedIn) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                return@setOnClickListener
            }

            if (!viewModel.state.value.isApplied) {
                viewModel.apply()
            }

            // kao u tvom kodu: nakon apply otvori link
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        lifecycleScope.launch {
            viewModel.state.collectLatest { s ->
                if (s.error != null) {
                    Toast.makeText(this@InternshipDetailsActivity, s.error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }

                val internship = s.internship
                if (internship != null) {
                    bindInternship(internship)
                }

                cbApplied.isChecked = s.isApplied

                btnApply.text = if (s.isApplied) "Prijavljeno" else "Prijavi se"
                btnApply.isEnabled = !s.isApplied

                updateFavoriteIcon(s.isFavorite)
            }
        }

        viewModel.load(internshipId)
    }

    private fun bindInternship(p: hr.foi.air.otpstudent.domain.model.Internship) {
        tvTitle.text = p.title.ifBlank { "-" }
        tvCompany.text = p.company.ifBlank { "-" }

        val compensation = when {
            p.hourlyRateMax > 0.0 && p.hourlyRateMax != p.hourlyRate ->
                "${p.hourlyRate}–${p.hourlyRateMax} €/h"
            p.hourlyRate > 0.0 ->
                "${p.hourlyRate} €/h"
            else -> "-"
        }
        tvComp.text = compensation

        tvLoc.text = p.location.ifBlank { "-" }

        val durationOrExpires = p.expiresAt?.toDate()?.let { date ->
            "do " + SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(date)
        } ?: "-"
        tvDur.text = durationOrExpires

        tvDesc.text = p.description.ifBlank { "-" }

        val areas = if (p.requirements.isNotEmpty()) p.requirements.joinToString(", ") else "-"
        tvAreas.text = areas
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        ivFavorite.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
        ivFavorite.setColorFilter(
            getColor(if (isFavorite) R.color.otp_green_dark else R.color.otp_grey)
        )
    }

    private inner class VmFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InternshipDetailsViewModel::class.java)) {
                val repo = AppModule.internshipRepository
                val uidProvider = { FirebaseAuth.getInstance().currentUser?.uid }
                @Suppress("UNCHECKED_CAST")
                return InternshipDetailsViewModel(repo, uidProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
