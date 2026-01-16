package hr.foi.air.otpstudent.ui.points

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.di.AppModule
import hr.foi.air.otpstudent.domain.model.QuizQuestion
import android.view.LayoutInflater
import android.widget.ImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private val vm: LoyaltyViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    ) {
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
        val btnAnswer1 = view.findViewById<Button>(R.id.btnAnswer1)
        val btnAnswer2 = view.findViewById<Button>(R.id.btnAnswer2)
        val btnAnswer3 = view.findViewById<Button>(R.id.btnAnswer3)
        val btnAnswer4 = view.findViewById<Button>(R.id.btnAnswer4)

        val btnClose = view.findViewById<Button>(R.id.btnClose)

        btnClose.setOnClickListener { parentFragmentManager.popBackStack() }

        setButtonsEnabled(false, btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4)

        vm.quizQuestion.observe(viewLifecycleOwner) { q ->
            loadedQuestion = q

            if (q == null) {
                tvQuestion.text = "Kviz trenutno nije dostupan."
                btnAnswer1.visibility = View.GONE
                btnAnswer2.visibility = View.GONE
                btnAnswer3.visibility = View.GONE
                return@observe
            }

            tvQuestion.text = q.text

            val options = q.options
            applyOption(btnAnswer1, options, 0)
            applyOption(btnAnswer2, options, 1)
            applyOption(btnAnswer3, options, 2)
            applyOption(btnAnswer4, options, 3)

            btnAnswer1.setOnClickListener { submitAnswer(selectedIndex = 0) }
            btnAnswer2.setOnClickListener { submitAnswer(selectedIndex = 1) }
            btnAnswer3.setOnClickListener { submitAnswer(selectedIndex = 2) }
            btnAnswer4.setOnClickListener { submitAnswer(selectedIndex = 3) }

            setButtonsEnabled(true, btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4)
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
                ivIcon.setImageResource(R.drawable.ic_challenge_default) // stavi neki "check" ako imaš
                tvTitle.text = "Točno!"
                tvMessage.text = "Osvojio si ${result.pointsAwarded} bodova!"
            } else {
                ivIcon.setImageResource(R.drawable.ic_challenge_default) // stavi neki "x" ako imaš
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



    private fun applyOption(btn: Button, options: List<String>, index: Int) {
        val text = options.getOrNull(index)
        if (text.isNullOrBlank()) {
            btn.visibility = View.GONE
        } else {
            btn.visibility = View.VISIBLE
            btn.text = text
        }
    }

    private fun setButtonsEnabled(enabled: Boolean, vararg buttons: Button) {
        buttons.forEach { it.isEnabled = enabled }
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
