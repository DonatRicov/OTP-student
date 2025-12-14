package hr.foi.air.core.auth

import java.util.concurrent.CopyOnWriteArrayList

object AuthRegistry {

    private val plugins = CopyOnWriteArrayList<AuthPlugin>()

    fun register(plugin: AuthPlugin) {
        val id = plugin.uiSpec().id
        plugins.removeAll { it.uiSpec().id == id }
        plugins.add(plugin)
    }

    fun available(): List<AuthPlugin> =
        plugins.sortedBy { it.uiSpec().order }
}
