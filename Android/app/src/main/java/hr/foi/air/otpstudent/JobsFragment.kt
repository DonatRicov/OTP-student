package hr.foi.air.otpstudent

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hr.foi.air.otpstudent.model.Job

class JobsFragment : Fragment(R.layout.fragment_jobs) {

    private lateinit var db: FirebaseFirestore

    private lateinit var adapter: JobAdapter
    private lateinit var rvJobs: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var tvMyApplications: TextView

    private lateinit var tvEmpty: TextView
    private var allJobs: List<Job> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        db = FirebaseFirestore.getInstance()

        rvJobs = view.findViewById(R.id.rvJobs)
        etSearch = view.findViewById(R.id.etSearch)
        tvMyApplications = view.findViewById(R.id.tvMyApplications)

        adapter = JobAdapter { job ->
            Toast.makeText(requireContext(), job.title, Toast.LENGTH_SHORT).show()
        }

        rvJobs.layoutManager = LinearLayoutManager(requireContext())
        rvJobs.adapter = adapter

        // search
        etSearch.addTextChangedListener { text ->
            filterJobs(text?.toString().orEmpty())
        }

        // Moje prijave - filter to be added
        tvMyApplications.setOnClickListener {
            val prijavljeni = allJobs.filter { it.isApplied }
            adapter.submitList(prijavljeni)
        }

        loadJobs()
    }

    private fun loadJobs() {
        db.collection("jobs")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                Toast.makeText(
                    requireContext(),
                    "Dohvaćeno poslova: ${snapshot.size()}",
                    Toast.LENGTH_SHORT
                ).show()

                if (snapshot.isEmpty) {
                    adapter.submitList(emptyList())
                    tvEmpty.visibility = View.VISIBLE
                    rvJobs.visibility = View.GONE
                    return@addOnSuccessListener
                }


                val jobs = snapshot.documents.map { doc ->
                    Job(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        company = doc.getString("company") ?: "",
                        location = doc.getString("location") ?: "",
                        hourlyRate = (doc.getDouble("hourlyRate") ?: 0.0),
                        hourlyRateMax = (doc.getDouble("hourlyRateMax") ?: 0.0),
                        applicantsCount = (doc.getLong("applicantsCount") ?: 0L).toInt(),
                        postedAt = doc.getTimestamp("postedAt"),
                        expiresAt = doc.getTimestamp("expiresAt"),
                        isClosed = doc.getBoolean("isClosed") ?: false,
                        isApplied = doc.getBoolean("isApplied") ?: false,
                        isFavorite = doc.getBoolean("isFavorite") ?: false,
                        description = doc.getString("description") ?: ""
                    )
                }
                tvEmpty.visibility = View.GONE
                rvJobs.visibility = View.VISIBLE

                allJobs = jobs
                adapter.submitList(jobs)


            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Greška pri dohvaćanju poslova: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun filterJobs(query: String) {
        if (query.isBlank()) {
            adapter.submitList(allJobs)
            return
        }

        val lower = query.lowercase()
        val filtered = allJobs.filter { job ->
            job.title.lowercase().contains(lower) ||
                    job.location.lowercase().contains(lower) ||
                    job.company.lowercase().contains(lower)
        }

        adapter.submitList(filtered)
    }
}
