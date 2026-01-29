package hr.foi.air.otpstudent.ui.chat.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.ui.chat.model.ChatConversation

class ChatHistoryAdapter(
    private val onClick: (ChatConversation) -> Unit
) : RecyclerView.Adapter<ChatHistoryAdapter.VH>() {

    private val items = mutableListOf<ChatConversation>()

    fun submitList(list: List<ChatConversation>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_history, parent, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View, private val onClick: (ChatConversation) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val tvTitle = itemView.findViewById<TextView>(R.id.tvHistoryTitle)

        fun bind(item: ChatConversation) {
            val title = item.title.ifBlank {
                itemView.context.getString(R.string.chat_history_empty_title)
            }
            tvTitle.text = title
            itemView.setOnClickListener { onClick(item) }
        }
    }

}
