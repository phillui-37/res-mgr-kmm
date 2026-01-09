package xyz.kgy_production.res_mgr.config

interface AppConfig {
    var serverUrl: String
    var clientName: String

    // Simple property based persistence usually
    fun save()
}

