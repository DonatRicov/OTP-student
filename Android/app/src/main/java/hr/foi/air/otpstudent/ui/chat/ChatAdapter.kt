package hr.foi.air.otpstudent.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.foi.air.otpstudent.R
import hr.foi.air.otpstudent.ui.chat.model.ChatMessage

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ChatMessage>()

    fun submitList(list: List<ChatMessage>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position].fromUser) VIEW_USER else VIEW_BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_USER) {
            UserVH(inf.inflate(R.layout.item_chat_user, parent, false))
        } else {
            BotVH(inf.inflate(R.layout.item_chat_bot, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = items[position]
        when (holder) {
            is UserVH -> holder.bind(msg)
            is BotVH -> holder.bind(msg)
        }
    }

    override fun getItemCount(): Int = items.size

    private class BotVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tv = v.findViewById<TextView>(R.id.tvBotMsg)
        fun bind(m: ChatMessage) { tv.text = m.text }
    }

    private class UserVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tv = v.findViewById<TextView>(R.id.tvUserMsg)
        fun bind(m: ChatMessage) { tv.text = m.text }
    }

    private companion object {
        const val VIEW_USER = 1
        const val VIEW_BOT = 2
    }
}
