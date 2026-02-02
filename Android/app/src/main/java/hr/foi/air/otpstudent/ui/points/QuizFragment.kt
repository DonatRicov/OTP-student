package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import hr.foi.air.otpstudent.domain.model.QuizQuestion

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private val vm: LoyaltyViewModel by activityViewModels {
        LoyaltyViewModelFactory(AppModule.loyaltyRepository)
    }

    private lateinit var challengeId: String
    private var loadedQuestion: QuizQuestion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        challengeId = requireArguments().getString(ARG_CHALLENGE_ID)
            ?: error("QuizFragment requires challengeId")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvQuestion = view.findViewById<TextView>(R.id.tvQuestion)

        val cardAnswer1 = view.findViewById<MaterialCardView>(R.id.btnAnswer1)
        val cardAnswer2 = view.findViewById<MaterialCardView>(R.id.btnAnswer2)
        val cardAnswer3 = view.findViewById<MaterialCardView>(R.id.btnAnswer3)
        val cardAnswer4 = view.findViewById<MaterialCardView>(R.id.btnAnswer4)

        val btnClose = view.findViewById<Button>(R.id.btnClose)
        btnClose.setOnClickListener { parentFragmentManager.popBackStack() }

        setViewsEnabled(false, cardAnswer1, cardAnswer2, cardAnswer3, cardAnswer4)

        vm.quizQuestion.observe(viewLifecycleOwner) { q ->
            loadedQuestion = q

            if (q == null) {
                tvQuestion.text = "Kviz trenutno nije dostupan."
                cardAnswer1.visibility = View.GONE
                cardAnswer2.visibility = View.GONE
                cardAnswer3.visibility = View.GONE
                cardAnswer4.visibility = View.GONE
                return@observe
            }

            tvQuestion.text = q.text

            val options = q.options

            applyOption(cardAnswer1, R.id.tvAnswer1, options, 0)
            applyOption(cardAnswer2, R.id.tvAnswer2, options, 1)
            applyOption(cardAnswer3, R.id.tvAnswer3, options, 2)
            applyOption(cardAnswer4, R.id.tvAnswer4, options, 3)

            cardAnswer1.setOnClickListener { submitAnswer(selectedIndex = 0) }
            cardAnswer2.setOnClickListener { submitAnswer(selectedIndex = 1) }
            cardAnswer3.setOnClickListener { submitAnswer(selectedIndex = 2) }
            cardAnswer4.setOnClickListener { submitAnswer(selectedIndex = 3) }

            setViewsEnabled(true, cardAnswer1, cardAnswer2, cardAnswer3, cardAnswer4)
        }

        vm.quizSubmitResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe

            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_quiz_result, null, false)

            val ivIcon = dialogView.findViewById<ImageView>(R.id.ivIcon)
            val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
            val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
            val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

            if (result.correct) {
                ivIcon.setImageResource(R.drawable.ic_challenge_default)
                tvTitle.text = "Točno!"
                tvMessage.text = "Osvojio si ${result.pointsAwarded} bodova!"
            } else {
                ivIcon.setImageResource(R.drawable.ic_challenge_default)
                tvTitle.text = "Ups!"
                tvMessage.text = "Krivi odgovor."
            }

            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()

            btnOk.setOnClickListener {
                dialog.dismiss()
                vm.clearQuizSubmitResult()
                parentFragmentManager.popBackStack()
            }

            dialog.show()
        }

        vm.loadQuizQuestion(challengeId)
    }

    private fun submitAnswer(selectedIndex: Int) {
        vm.submitQuizAnswer(challengeId, selectedIndex)
    }

    private fun applyOption(
        card: MaterialCardView,
        textViewId: Int,
        options: List<String>,
        index: Int
    ) {
        val text = options.getOrNull(index)
        if (text.isNullOrBlank()) {
            card.visibility = View.GONE
        } else {
            card.visibility = View.VISIBLE
            val tv = card.findViewById<TextView>(textViewId)
            tv.text = text
        }
    }

    private fun setViewsEnabled(enabled: Boolean, vararg views: View) {
        views.forEach { it.isEnabled = enabled }
    }

    companion object {
        private const val ARG_CHALLENGE_ID = "challenge_id"

        fun newInstance(challengeId: String): QuizFragment {
            return QuizFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHALLENGE_ID, challengeId)
                }
            }
        }
    }
}
