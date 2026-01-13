package hr.foi.air.otpstudent.ui.points

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.domain.model.ChallengeWithState

class ChallengesAdapter(
    private val onClaim: (challengeId: String) -> Unit
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
        return VH(v, onClaim)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View, private val onClaim: (String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val points: TextView = itemView.findViewById(R.id.tvPoints)
        private val status: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnClaim: Button = itemView.findViewById(R.id.btnClaim)

        fun bind(item: ChallengeWithState) {
            val ch = item.challenge
            val st = item.state?.status ?: "ACTIVE"

            title.text = ch.title
            points.text = "Bodovi: ${ch.rewardPoints}"

            when (st) {
                "COMPLETED_PENDING_CLAIM" -> {
                    status.text = "Završeno - čeka preuzimanje"
                    btnClaim.visibility = View.VISIBLE
                    btnClaim.setOnClickListener { onClaim(ch.id) }
                }
                "CLAIMED" -> {
                    status.text = "Preuzeto ✅"
                    btnClaim.visibility = View.GONE
                }
                "EXPIRED" -> {
                    status.text = "Isteklo ⏳"
                    btnClaim.visibility = View.GONE
                }
                else -> {
                    status.text = "Aktivno"
                    btnClaim.visibility = View.GONE
                }
            }
        }
    }
}
