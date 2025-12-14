package hr.foi.air.otpstudent

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
import android.content.Intent
import androidx.navigation.fragment.findNavController
import hr.foi.air.otpstudent.R
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth



class JobsFragment : Fragment(R.layout.fragment_jobs) {

    private lateinit var db: FirebaseFirestore

    private lateinit var adapter: JobAdapter
    private lateinit var rvJobs: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var tvMyApplications: TextView
    private lateinit var btnFilter: MaterialButton
    private lateinit var tvActiveFilters: TextView

    private lateinit var tvEmpty: TextView
    private var allJobs: List<Job> = emptyList()

    // definicija mogućih filtera
    private enum class JobFilter {
        ACTIVE,
        APPLIED,
        FAVORITE,
        BEST_PAID
    }

    // trenutno aktivni filteri (moze vise)
    private val activeFilters = mutableSetOf<JobFilter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvJobs = view.findViewById(R.id.rvJobs)
        etSearch = view.findViewById(R.id.etSearch)
        tvMyApplications = view.findViewById(R.id.btnFavourites)

        tvMyApplications.setOnClickListener {
            findNavController().navigate(R.id.nav_jobs_favorites)
        }




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

        // search
        etSearch.addTextChangedListener { text ->
            applyAllFilters(text?.toString().orEmpty())
        }


        // FILTER gumb
        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        loadJobsWithUserStatus()
    }

    private fun showFilterDialog() {
        val labels = arrayOf(
            "Aktivni poslovi",
            "Moje prijave",
            "Favoriti",
            "Najbolje plaćeni"
        )

        val filterMap = arrayOf(
            JobFilter.ACTIVE,
            JobFilter.APPLIED,
            JobFilter.FAVORITE,
            JobFilter.BEST_PAID
        )

        val checkedItems = BooleanArray(labels.size) { index ->
            activeFilters.contains(filterMap[index])
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtriraj poslove")
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

                if (JobFilter.FAVORITE in activeFilters) {
                    ok = ok && job.isFavorite
                }
                if (JobFilter.BEST_PAID in activeFilters) {
                    val rate = if (job.hourlyRateMax > 0.0) job.hourlyRateMax else job.hourlyRate
                    ok = ok && rate >= 8.0   // placeholder prag
                }

                ok
            }
        }

        adapter.submitList(filtered)

        // empty state
        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvJobs.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvJobs.visibility = View.VISIBLE
        }

        // updateaj tekst
        updateActiveFiltersLabel()
    }

    private fun updateActiveFiltersLabel() {
        if (activeFilters.isEmpty()) {
            tvActiveFilters.visibility = View.GONE
            return
        }

        // ako su uključeni svi filteri
        if (activeFilters.size == JobFilter.values().size) {
            tvActiveFilters.visibility = View.VISIBLE
            tvActiveFilters.text = "Svi su filteri primjenjeni"
            return
        }

        val parts = mutableListOf<String>()

        if (JobFilter.ACTIVE in activeFilters) parts.add("aktivni")
        if (JobFilter.APPLIED in activeFilters) parts.add("moje prijave")
        if (JobFilter.FAVORITE in activeFilters) parts.add("favoriti")
        if (JobFilter.BEST_PAID in activeFilters) parts.add("najbolje plaćeni")

        tvActiveFilters.visibility = View.VISIBLE
        tvActiveFilters.text = "Odabrani: " + parts.joinToString(", ")
    }


    private fun loadJobsWithUserStatus() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        val jobsTask = db.collection("jobs")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .get()

        // ako user nije prijavljen fallback
        if (uid == null) {
            jobsTask.addOnSuccessListener { snapshot ->
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
                        isApplied = false,
                        isFavorite = false,
                        description = doc.getString("description") ?: ""
                    )
                }

                allJobs = jobs
                markExpiredJobsLocally()
                applyAllFilters(etSearch.text?.toString().orEmpty())
            }
            return
        }

        val appliedTask = db.collection("users").document(uid)
            .collection("applied")
            .get()

        val favoritesTask = db.collection("users").document(uid)
            .collection("favorites")
            .get()

        Tasks.whenAllSuccess<Any>(jobsTask, appliedTask, favoritesTask)
            .addOnSuccessListener { results ->
                val jobsSnap = results[0] as com.google.firebase.firestore.QuerySnapshot
                val appliedSnap = results[1] as com.google.firebase.firestore.QuerySnapshot
                val favoritesSnap = results[2] as com.google.firebase.firestore.QuerySnapshot

                val appliedIds = appliedSnap.documents.map { it.id }.toHashSet()
                val favoriteIds = favoritesSnap.documents.map { it.id }.toHashSet()

                val jobs = jobsSnap.documents.map { doc ->
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
                        isApplied = appliedIds.contains(doc.id),
                        isFavorite = favoriteIds.contains(doc.id),
                        description = doc.getString("description") ?: ""
                    )
                }

                allJobs = jobs
                markExpiredJobsLocally()
                applyAllFilters(etSearch.text?.toString().orEmpty())
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Greška pri dohvaćanju statusa: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }


    private fun markExpiredJobsLocally() {
        val now = com.google.firebase.Timestamp.now()

        val updated = allJobs.map { job ->
            val exp = job.expiresAt
            if (exp != null && exp <= now && !job.isClosed) {
                db.collection("jobs").document(job.id).update("isClosed", true)
                job.copy(isClosed = true)
            } else job
        }

        allJobs = updated
    }


    override fun onResume() {
        super.onResume()
        loadJobsWithUserStatus()
    }



}
