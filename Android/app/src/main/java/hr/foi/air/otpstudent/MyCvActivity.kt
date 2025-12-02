package hr.foi.air.otpstudent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.firebase.auth.FirebaseAuth

class MyCvActivity : AppCompatActivity() {

    // promjeniti za koristenje baze
    private lateinit var repository: CvRepository
    private lateinit var adapter: CvAdapter
    private lateinit var auth: FirebaseAuth

    // PDF Picker
    private val pdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadCv(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cv)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            // fallback ako se ne varam
            Toast.makeText(this, "Molimo prijavite se.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        repository = FirebaseCvRepository(currentUser.uid)

        setupRecyclerView()

        findViewById<Button>(R.id.btnUploadPdf).setOnClickListener {
            pdfLauncher.launch("application/pdf")
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        loadCvs()
    }


    private fun setupRecyclerView() {
        adapter = CvAdapter(
            emptyList(),
            onItemClick = { cv -> openPdf(cv) },
            onDeleteClick = { cv -> deleteCv(cv) }
        )
        val rv = findViewById<RecyclerView>(R.id.recyclerViewCvs)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun uploadCv(uri: Uri) {
        lifecycleScope.launch {
            val fileName = "cv_${System.currentTimeMillis()}.pdf"
            val downloadUrl = repository.saveFile(uri, fileName)

            if (downloadUrl != null) {
                val uid = auth.currentUser?.uid ?: return@launch

                val newCv = CvDocument(
                    id = UUID.randomUUID().toString(),
                    userId = uid,
                    fileName = fileName,
                    fileUrl = downloadUrl,
                    uploaderName = "Student",
                    timestamp = System.currentTimeMillis()
                )

                repository.addCv(newCv)
                loadCvs()
                Toast.makeText(this@MyCvActivity, "Dodan životopis!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MyCvActivity, "Greška pri spremanju.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun loadCvs() {
        lifecycleScope.launch {
            val list = repository.getAllCvs()
            adapter.updateData(list.sortedByDescending { it.timestamp })
        }
    }

    private fun deleteCv(cv: CvDocument) {
        lifecycleScope.launch {
            repository.deleteCv(cv)
            loadCvs()
        }
    }

    private fun openPdf(cv: CvDocument) {
        val uri = Uri.parse(cv.fileUrl)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Nemate aplikaciju za PDF (pokušajte kroz browser).", Toast.LENGTH_SHORT).show()

            // fallback
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(browserIntent)
        }
    }

}
