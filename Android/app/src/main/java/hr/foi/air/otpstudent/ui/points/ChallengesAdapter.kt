package hr.foi.air.otpstudent.ui.points

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.domain.model.ChallengeWithState
import android.widget.ImageView

class ChallengesAdapter(
    private val onClaim: (challengeId: String) -> Unit,
    private val onOpenQuiz: (challengeId: String) -> Unit

) : RecyclerView.Adapter<ChallengesAdapter.VH>() {

    private val items = mutableListOf<ChallengeWithState>()

    fun submitList(newItems: List<ChallengeWithState>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return VH(v, onClaim, onOpenQuiz)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View, private val onClaim: (String) -> Unit, private val onOpenQuiz: (String) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {

        private val btnStart: Button = itemView.findViewById(R.id.btnStart)
        private val cardLocked: View = itemView.findViewById(R.id.cardLocked)
        private val cardClaim: View = itemView.findViewById(R.id.cardClaim)

        private val tvTitleLocked: TextView = itemView.findViewById(R.id.tvTitleLocked)
        private val tvDescLocked: TextView = itemView.findViewById(R.id.tvDescLocked)
        private val tvBadgePoints: TextView = itemView.findViewById(R.id.tvBadgePoints)
        private val ivIconLocked: ImageView = itemView.findViewById(R.id.ivIconLocked)

        private val tvTitleClaim: TextView = itemView.findViewById(R.id.tvTitleClaim)
        private val tvDescClaim: TextView = itemView.findViewById(R.id.tvDescClaim)
        private val tvPointsOnButton: TextView = itemView.findViewById(R.id.tvPointsOnButton)
        private val ivIconClaim: ImageView = itemView.findViewById(R.id.ivIconClaim)
        private val btnClaim: Button = itemView.findViewById(R.id.btnClaim)

        fun bind(item: ChallengeWithState) {
            val ch = item.challenge
            val isQuiz = ch.type.trim().uppercase() == "QUIZ_WEEKLY"

            val stRaw = item.state?.status
            val st = (stRaw ?: "ACTIVE").trim().uppercase()

            val pointsText = "+ ${ch.rewardPoints}"
            val iconRes = iconFromKey(ch.iconKey)

            tvTitleLocked.text = ch.title
            tvBadgePoints.text = pointsText
            ivIconLocked.setImageResource(iconRes)

            tvTitleClaim.text = ch.title
            tvPointsOnButton.text = pointsText
            ivIconClaim.setImageResource(iconRes)

            val desc = ch.description?.trim().orEmpty()
            tvDescLocked.text = desc
            tvDescClaim.text = desc

            tvDescLocked.visibility = if (desc.isBlank()) View.GONE else View.VISIBLE
            tvDescClaim.visibility = if (desc.isBlank()) View.GONE else View.VISIBLE

            cardLocked.visibility = View.GONE
            cardClaim.visibility = View.GONE
            btnStart.visibility = View.GONE
            btnClaim.visibility = View.GONE

            when (st) {
                "COMPLETED_PENDING_CLAIM" -> {
                    cardClaim.visibility = View.VISIBLE
                    btnClaim.visibility = View.VISIBLE
                    btnClaim.setOnClickListener { onClaim(ch.id) }
                }

                "CLAIMED" -> {
                    cardClaim.visibility = View.VISIBLE
                }

                "EXPIRED" -> {
                    cardLocked.visibility = View.VISIBLE
                }

                else -> {
                    cardLocked.visibility = View.VISIBLE

                    if (isQuiz) {
                        btnStart.visibility = View.VISIBLE
                        btnStart.text = "RIJEŠI KVIZ"
                        btnStart.setOnClickListener { onOpenQuiz(ch.id) }
                    } else {
                        btnStart.visibility = View.GONE
                    }
                }

            }

        }

        private fun iconFromKey(key: String): Int {
            return when (key.lowercase()) {
                "pig" -> R.drawable.ic_challenge_default
                "quiz" -> R.drawable.ic_challenge_quiz
                "profile" -> R.drawable.ic_challenge_profile
                "explore" -> R.drawable.ic_challenge_explore
                "apply" -> R.drawable.ic_challenge_apply
                "login" -> R.drawable.ic_challenge_login
                else -> R.drawable.ic_challenge_default
            }
        }
    }

}
