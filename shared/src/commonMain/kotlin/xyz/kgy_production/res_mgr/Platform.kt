package xyz.kgy_production.res_mgr

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform