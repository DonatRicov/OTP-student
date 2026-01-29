package hr.foi.air.otpstudent.ui.points

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.domain.model.RedeemedReward

class RedeemedRewardsAdapter(
    private val onOpen: (rewardId: String, redemptionId: String) -> Unit
) : RecyclerView.Adapter<RedeemedRewardsAdapter.VH>() {

    private val items = mutableListOf<RedeemedReward>()

    fun submit(list: List<RedeemedReward>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false)
        return VH(v, onOpen)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(
        itemView: View,
        private val onOpen: (rewardId: String, redemptionId: String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val desc: TextView? = itemView.findViewById(R.id.tvDesc)
        private val cost: TextView = itemView.findViewById(R.id.tvCost)
        private val btn: View = itemView.findViewById(R.id.btnRedeem)
        private val image: ImageView = itemView.findViewById(R.id.advertisement)

        fun bind(item: RedeemedReward) {
            val r = item.reward

            title.text = r.title
            desc?.text = r.description

            cost.text = "Preuzeto"
            btn.isEnabled = true
            btn.alpha = 1f

            val img = r.imageUrl
            if (!img.isNullOrBlank() && img.startsWith("gs://")) {
                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(img)
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

            itemView.setOnClickListener {
                onOpen(r.id, item.redemptionId)
            }
        }
    }
}
