package xyz.kgy_production.res_mgr.config

import java.util.prefs.Preferences

class JvmAppConfig : AppConfig {
    private val prefs = Preferences.userNodeForPackage(JvmAppConfig::class.java)

    override var serverUrl: String
        get() = prefs.get("SERVER_URL", "http://localhost:8080")
        set(value) { prefs.put("SERVER_URL", value) }

    override var clientName: String
        get() = prefs.get("CLIENT_NAME", "Desktop-Client-" + System.getProperty("user.name"))
        set(value) { prefs.put("CLIENT_NAME", value) }

    override fun save() {
        try {
            prefs.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

