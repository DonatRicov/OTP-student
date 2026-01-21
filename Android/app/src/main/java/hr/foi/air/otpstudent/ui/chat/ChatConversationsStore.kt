package hr.foi.air.otpstudent.ui.chat

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hr.foi.air.otpstudent.ui.chat.model.ChatConversation
import java.util.UUID

class ChatConversationsStore(
    context: Context,
    private val gson: Gson = Gson()
) {
    private val prefs = context.getSharedPreferences("chat_history", Context.MODE_PRIVATE)

    fun loadAll(key: String): MutableList<ChatConversation> {
        val json = prefs.getString(key, null) ?: return mutableListOf()
        val type = object : TypeToken<List<ChatConversation>>() {}.type
        val list = runCatching { gson.fromJson<List<ChatConversation>>(json, type) }
            .getOrDefault(emptyList())
        return list.toMutableList()
    }

    fun saveAll(key: String, conversations: List<ChatConversation>) {
        prefs.edit().putString(key, gson.toJson(conversations)).apply()
    }

    fun createNew(key: String): ChatConversation {
        val conversations = loadAll(key)
        val conv = ChatConversation(id = UUID.randomUUID().toString())
        conversations.add(0, conv) // newest first
        saveAll(key, conversations)
        return conv
    }

    fun getById(key: String, id: String): ChatConversation? {
        return loadAll(key).firstOrNull { it.id == id }
    }

    fun upsert(key: String, updated: ChatConversation) {
        val conversations = loadAll(key)
        val idx = conversations.indexOfFirst { it.id == updated.id }
        if (idx >= 0) conversations[idx] = updated else conversations.add(0, updated)
        saveAll(key, conversations.sortedByDescending { it.createdAt })
    }

    fun clearAll(key: String) {
        prefs.edit().remove(key).apply()
    }
}
