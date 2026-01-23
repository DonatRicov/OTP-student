package hr.foi.air.otpstudent.ui.points

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.domain.model.Reward
import com.bumptech.glide.Glide
import android.widget.ImageView
import com.google.firebase.storage.FirebaseStorage

class RewardsAdapter(
    private val onOpenDetails: (rewardId: String) -> Unit
) : RecyclerView.Adapter<RewardsAdapter.VH>() {

    private val items = mutableListOf<Reward>()
    private var points: Long = 0L

    fun submit(rewards: List<Reward>, currentPoints: Long) {
        items.clear()
        items.addAll(rewards)
        points = currentPoints
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false)
        return VH(v, onOpenDetails)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], points)
    }

    override fun getItemCount(): Int = items.size

    class VH(
        itemView: View,
        private val onOpenDetails: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val desc: TextView? = itemView.findViewById(R.id.tvDesc)
        private val cost: TextView = itemView.findViewById(R.id.tvCost)
        private val btn: View = itemView.findViewById(R.id.btnRedeem)
        private val image: ImageView = itemView.findViewById(R.id.advertisement)

        fun bind(item: Reward, points: Long) {
            title.text = item.title
            desc?.text = item.description
            cost.text = item.costPoints.toString()

            val img = item.imageUrl

            if (!img.isNullOrBlank() && img.startsWith("gs://")) {
                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(img)

                image.setImageDrawable(null)

                ref.downloadUrl
                    .addOnSuccessListener { uri ->
                        Glide.with(itemView)
                            .load(uri)
                            .placeholder(R.drawable.placeholder_reward)
                            .error(R.drawable.placeholder_reward)
                            .into(image)
                    }
                    .addOnFailureListener {
                        image.setImageResource(R.drawable.placeholder_reward)
                    }
            } else {
                Glide.with(itemView)
                    .load(img)
                    .placeholder(R.drawable.placeholder_reward)
                    .error(R.drawable.placeholder_reward)
                    .into(image)
            }

            itemView.setOnClickListener { onOpenDetails(item.id) }
            // Dodaj opis
            val canRedeem = points >= item.costPoints
            btn.isEnabled = canRedeem
            btn.alpha = if (canRedeem) 1f else 0.5f
        }
    }
}

