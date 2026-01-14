package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.data.repository.FirebaseLoyaltyRepositoryImpl
import hr.foi.air.otpstudent.data.source.remote.FirebaseLoyaltyRemoteDataSource
import hr.foi.air.otpstudent.ui.points.ChallengesAdapter

class ChallengesFragment : Fragment(R.layout.fragment_challenges) {

    private lateinit var vm: LoyaltyViewModel
    private lateinit var adapter: ChallengesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progress = view.findViewById<ProgressBar>(R.id.progress)
        val error = view.findViewById<TextView>(R.id.tvError)
        val rv = view.findViewById<RecyclerView>(R.id.rvChallenges)

        adapter = ChallengesAdapter(onClaim = { vm.claim(it) })
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val repo = FirebaseLoyaltyRepositoryImpl(
            FirebaseAuth.getInstance(),
            FirebaseLoyaltyRemoteDataSource(FirebaseFirestore.getInstance())
        )

        vm = ViewModelProvider(this, LoyaltyViewModelFactory(repo))[LoyaltyViewModel::class.java]


        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoyaltyUiState.Loading -> {
                    progress.visibility = View.VISIBLE
                    error.visibility = View.GONE
                }
                is LoyaltyUiState.Success -> {
                    progress.visibility = View.GONE
                    error.visibility = View.GONE
                    adapter.submitList(state.items)
                }
                is LoyaltyUiState.Error -> {
                    progress.visibility = View.GONE
                    error.visibility = View.VISIBLE
                    error.text = state.message
                }
            }
        }

        vm.load()
    }
}
