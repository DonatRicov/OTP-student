package hr.foi.air.otpstudent

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hr.foi.air.otpstudent.model.Job
import androidx.navigation.fragment.findNavController

class JobsFavoritesFragment : Fragment(R.layout.fragment_jobs_favorites) {

    private lateinit var db: FirebaseFirestore

    private lateinit var adapter: JobAdapter
    private lateinit var rvJobs: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnFilter: MaterialButton
    private lateinit var tvActiveFilters: TextView
    private lateinit var tvEmpty: TextView

    private var allJobs: List<Job> = emptyList()

    private enum class JobFilter {
        ACTIVE,
        APPLIED,
        BEST_PAID
    }

    private val activeFilters = mutableSetOf<JobFilter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnFavourites = view.findViewById<TextView>(R.id.btnFavourites)
        btnFavourites.setOnClickListener {
            findNavController().popBackStack()
        }


        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvJobs = view.findViewById(R.id.rvJobs)
        etSearch = view.findViewById(R.id.etSearch)
        btnFilter = view.findViewById(R.id.btnFilter)
        tvActiveFilters = view.findViewById(R.id.tvActiveFilters)


        db = FirebaseFirestore.getInstance()

        adapter = JobAdapter { job ->
            val intent = Intent(requireContext(), JobDetailsActivity::class.java)
            intent.putExtra("JOB_ID", job.id)
            startActivity(intent)
        }

        rvJobs.layoutManager = LinearLayoutManager(requireContext())
        rvJobs.adapter = adapter

        etSearch.addTextChangedListener { text ->
            applyAllFilters(text?.toString().orEmpty())
        }

        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        loadJobsWithUserStatus()
    }

    private fun showFilterDialog() {
        val labels = arrayOf(
            "Aktivni poslovi",
            "Moje prijave",
            "Najbolje plaćeni"
        )

        val filterMap = arrayOf(
            JobFilter.ACTIVE,
            JobFilter.APPLIED,
            JobFilter.BEST_PAID
        )

        val checkedItems = BooleanArray(labels.size) { index ->
            activeFilters.contains(filterMap[index])
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtriraj favorite")
            .setMultiChoiceItems(labels, checkedItems) { _, which, isChecked ->
                val filter = filterMap[which]
                if (isChecked) {
                    activeFilters.add(filter)
                } else {
                    activeFilters.remove(filter)
                }
            }
            .setNegativeButton("Odustani", null)
            .setPositiveButton("Primijeni") { _, _ ->
                applyAllFilters(etSearch.text?.toString().orEmpty())
            }
            .setNeutralButton("Očisti") { _, _ ->
                activeFilters.clear()
                applyAllFilters(etSearch.text?.toString().orEmpty())
            }
            .show()
    }

    private fun applyAllFilters(query: String) {

        var filtered = allJobs

        val lower = query.lowercase().trim()
        if (lower.isNotEmpty()) {
            filtered = filtered.filter { job ->
                job.title.lowercase().contains(lower) ||
                        job.location.lowercase().contains(lower) ||
                        job.company.lowercase().contains(lower)
            }
        }

        if (activeFilters.isNotEmpty()) {
            filtered = filtered.filter { job ->
                var ok = true

                if (JobFilter.ACTIVE in activeFilters) {
                    ok = ok && !job.isClosed
                }
                if (JobFilter.APPLIED in activeFilters) {
                    ok = ok && job.isApplied
                }

                if (JobFilter.BEST_PAID in activeFilters) {
                    val rate = if (job.hourlyRateMax > 0.0) job.hourlyRateMax else job.hourlyRate
                    ok = ok && rate >= 8.0
                }

                ok
            }
        }

        adapter.submitList(filtered)

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            if (allJobs.any { it.isFavorite }) {
                tvEmpty.text = "Nema rezultata pretrage."
            } else {
                tvEmpty.text = "Nemate spremljenih favorita."
            }
            rvJobs.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvJobs.visibility = View.VISIBLE
        }

        updateActiveFiltersLabel()
    }

    private fun updateActiveFiltersLabel() {
        if (activeFilters.isEmpty()) {
            tvActiveFilters.visibility = View.GONE
            return
        }

        if (activeFilters.size == JobFilter.values().size) {
            tvActiveFilters.visibility = View.VISIBLE
            tvActiveFilters.text = "Svi su filteri primjenjeni"
            return
        }

        val parts = mutableListOf<String>()
        if (JobFilter.ACTIVE in activeFilters) parts.add("aktivni")
        if (JobFilter.APPLIED in activeFilters) parts.add("moje prijave")
        if (JobFilter.BEST_PAID in activeFilters) parts.add("najbolje plaćeni")

        tvActiveFilters.visibility = View.VISIBLE
        tvActiveFilters.text = "Odabrani: " + parts.joinToString(", ")
    }

    private fun loadJobsWithUserStatus() {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: return

        val jobsTask = db.collection("jobs")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .get()

        val appliedTask = db.collection("users")
            .document(uid)
            .collection("applied")
            .get()

        val favoritesTask = db.collection("users")
            .document(uid)
            .collection("favorites")
            .get()

        com.google.android.gms.tasks.Tasks.whenAllSuccess<Any>(
            jobsTask,
            appliedTask,
            favoritesTask
        ).addOnSuccessListener { results ->

            val jobsSnap = results[0] as com.google.firebase.firestore.QuerySnapshot
            val appliedSnap = results[1] as com.google.firebase.firestore.QuerySnapshot
            val favoritesSnap = results[2] as com.google.firebase.firestore.QuerySnapshot

            val appliedIds = appliedSnap.documents.map { it.id }.toHashSet()
            val favoriteIds = favoritesSnap.documents.map { it.id }.toHashSet()

            //filtriranje favorita
            val jobs = jobsSnap.documents
                .filter { favoriteIds.contains(it.id) }
                .map { doc ->
                    Job(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        company = doc.getString("company") ?: "",
                        location = doc.getString("location") ?: "",
                        hourlyRate = doc.getDouble("hourlyRate") ?: 0.0,
                        hourlyRateMax = doc.getDouble("hourlyRateMax") ?: 0.0,
                        applicantsCount = (doc.getLong("applicantsCount") ?: 0L).toInt(),
                        postedAt = doc.getTimestamp("postedAt"),
                        expiresAt = doc.getTimestamp("expiresAt"),
                        isClosed = doc.getBoolean("isClosed") ?: false,
                        isApplied = appliedIds.contains(doc.id),
                        isFavorite = true,
                        description = doc.getString("description") ?: ""
                    )
                }

            allJobs = jobs
            applyAllFilters(etSearch.text?.toString().orEmpty())
        }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Greška pri dohvaćanju favorita: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }




}
