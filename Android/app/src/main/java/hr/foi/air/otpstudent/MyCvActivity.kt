package hr.foi.air.otpstudent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class MyCvActivity : AppCompatActivity() {

    // promjeniti za koristenje baze
    private lateinit var repository: CvRepository
    private lateinit var adapter: CvAdapter

    // PDF Picker
    private val pdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadCv(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cv)

        // Promjeniti za FIREBASE
        repository = MockCvRepository(this)

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
            val fileName = "cv_${System.currentTimeMillis()}.pdf" // Or extract real name
            val savedPath = repository.saveFile(uri, fileName)

            if (savedPath != null) {
                val newCv = CvDocument(
                    id = UUID.randomUUID().toString(),
                    fileName = fileName,
                    filePath = savedPath,
                    uploaderName = "Student",
                    timestamp = System.currentTimeMillis()
                )

                repository.addCv(newCv)
                loadCvs()
                Toast.makeText(this@MyCvActivity, "Dodan Zivotopis!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MyCvActivity, "Greska pri spremanju.", Toast.LENGTH_SHORT).show()
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
        val file = File(cv.filePath)
        if (!file.exists()) {
            Toast.makeText(this, "Datoteka ne postoji!", Toast.LENGTH_SHORT).show()
            return
        }


        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Nemate aplikaciju za PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
