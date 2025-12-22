package hr.foi.air.otpstudent

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PraksaFragment : Fragment(R.layout.fragment_praksa) {

    private lateinit var db: FirebaseFirestore

    private lateinit var rvPractices: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: TextInputEditText
    private lateinit var tvAllPractices: TextView

    private lateinit var btnFilter: MaterialButton
    private lateinit var tvActiveFilters: TextView

    private lateinit var adapter: PracticeAdapter
    private var allPractices: List<Practice> = emptyList()

    // filteri
    private enum class PracticeFilter {
        ACTIVE,
        APPLIED,
        FAVORITE
    }

    private val activeFilters = mutableSetOf<PracticeFilter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()


        rvPractices = view.findViewById(R.id.rvPractices)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        etSearch = view.findViewById(R.id.etSearch)
        tvAllPractices = view.findViewById(R.id.btnFavourites)
        btnFilter = view.findViewById(R.id.btnFilter)
        tvActiveFilters = view.findViewById(R.id.tvActiveFilters)

        tvAllPractices.setOnClickListener {
            // favoriti prakse ili sve prakse, tu ide navigacija
        }

        adapter = PracticeAdapter { practice ->
            val intent = PracticeDetailsActivity.newIntent(requireContext(), practice)
            startActivity(intent)
        }

        rvPractices.layoutManager = LinearLayoutManager(requireContext())
        rvPractices.adapter = adapter

        // search
        etSearch.addTextChangedListener { text ->
            applyAllFilters(text?.toString().orEmpty())
        }

        // filter dialog
        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        loadPracticesWithUserStatus()
    }

    private fun showFilterDialog() {
        val labels = arrayOf(
            "Aktivne prakse",
            "Moje prijave",
            "Favoriti"
        )

        val filterMap = arrayOf(
            PracticeFilter.ACTIVE,
            PracticeFilter.APPLIED,
            PracticeFilter.FAVORITE
        )

        val checkedItems = BooleanArray(labels.size) { index ->
            activeFilters.contains(filterMap[index])
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtriraj prakse")
            .setMultiChoiceItems(labels, checkedItems) { _, which, isChecked ->
                val filter = filterMap[which]
                if (isChecked) activeFilters.add(filter) else activeFilters.remove(filter)
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
        var filtered = allPractices

        // search
        val lower = query.lowercase().trim()
        if (lower.isNotEmpty()) {
            filtered = filtered.filter { p ->
                p.title.lowercase().contains(lower) ||
                        p.location.lowercase().contains(lower) ||
                        p.company.lowercase().contains(lower)
            }
        }

        // filteri
        if (activeFilters.isNotEmpty()) {
            filtered = filtered.filter { p ->
                var ok = true

                if (PracticeFilter.ACTIVE in activeFilters) {
                    ok = ok && !p.isClosed
                }
                if (PracticeFilter.APPLIED in activeFilters) {
                    ok = ok && p.isApplied
                }
                if (PracticeFilter.FAVORITE in activeFilters) {
                    ok = ok && p.isFavorite
                }

                ok
            }
        }

        adapter.submitList(filtered)

        // empty state
        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvPractices.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvPractices.visibility = View.VISIBLE
        }

        updateActiveFiltersLabel()
    }

    private fun updateActiveFiltersLabel() {
        if (activeFilters.isEmpty()) {
            tvActiveFilters.visibility = View.GONE
            return
        }

        if (activeFilters.size == PracticeFilter.values().size) {
            tvActiveFilters.visibility = View.VISIBLE
            tvActiveFilters.text = "Svi su filteri primjenjeni"
            return
        }

        val parts = mutableListOf<String>()
        if (PracticeFilter.ACTIVE in activeFilters) parts.add("aktivne")
        if (PracticeFilter.APPLIED in activeFilters) parts.add("moje prijave")
        if (PracticeFilter.FAVORITE in activeFilters) parts.add("favoriti")

        tvActiveFilters.visibility = View.VISIBLE
        tvActiveFilters.text = "Odabrani: " + parts.joinToString(", ")
    }

    private fun loadPracticesWithUserStatus() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        val practicesTask = db.collection("practice")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .get()

        // nije prijavljen -> sve false
        if (uid == null) {
            practicesTask
                .addOnSuccessListener { snapshot ->
                    val practices = snapshot.documents.map { doc ->
                        Practice(
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
                            isApplied = false,
                            isFavorite = false,
                            description = doc.getString("description") ?: "",
                            applyUrl = doc.getString("applyUrl") ?: "",
                            requirements = (doc.get("requirements") as? List<String>) ?: emptyList()
                        )
                    }

                    allPractices = practices
                    applyAllFilters(etSearch.text?.toString().orEmpty())
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Greška pri dohvaćanju praksi: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    applyAllFilters(etSearch.text?.toString().orEmpty())
                }
            return
        }

        // prijavljen -> dohvat applied + favorites
        val appliedTask = db.collection("users").document(uid)
            .collection("applied")
            .get()

        val favoritesTask = db.collection("users").document(uid)
            .collection("favorites")
            .get()

        Tasks.whenAllSuccess<Any>(practicesTask, appliedTask, favoritesTask)
            .addOnSuccessListener { results ->
                val practicesSnap = results[0] as com.google.firebase.firestore.QuerySnapshot
                val appliedSnap = results[1] as com.google.firebase.firestore.QuerySnapshot
                val favoritesSnap = results[2] as com.google.firebase.firestore.QuerySnapshot

                val appliedIds = appliedSnap.documents.map { it.id }.toHashSet()
                val favoriteIds = favoritesSnap.documents.map { it.id }.toHashSet()

                val practices = practicesSnap.documents.map { doc ->
                    Practice(
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
                        isFavorite = favoriteIds.contains(doc.id),
                        description = doc.getString("description") ?: "",
                        applyUrl = doc.getString("applyUrl") ?: "",
                        requirements = (doc.get("requirements") as? List<String>) ?: emptyList()
                    )
                }

                allPractices = practices
                applyAllFilters(etSearch.text?.toString().orEmpty())
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Greška pri dohvaćanju statusa: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                applyAllFilters(etSearch.text?.toString().orEmpty())
            }
    }

    override fun onResume() {
        super.onResume()
        loadPracticesWithUserStatus()
    }
}
