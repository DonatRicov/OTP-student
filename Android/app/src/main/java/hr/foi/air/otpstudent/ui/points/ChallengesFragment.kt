package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.R
import androidx.fragment.app.viewModels
import hr.foi.air.otpstudent.di.AppModule

class ChallengesFragment : Fragment(R.layout.fragment_challenges) {

    private val vm: LoyaltyViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    ) {
        LoyaltyViewModelFactory(AppModule.loyaltyRepository)
    }

    private lateinit var adapter: ChallengesAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progress = view.findViewById<ProgressBar>(R.id.progress)
        val error = view.findViewById<TextView>(R.id.tvError)
        val rv = view.findViewById<RecyclerView>(R.id.rvChallenges)

        adapter = ChallengesAdapter(
            onClaim = { vm.claim(it) },
            onOpenQuiz = { challengeId ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.pointsFragmentContainer, QuizFragment.newInstance(challengeId))
                    .addToBackStack("quiz")
                    .commit()
            }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter


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
