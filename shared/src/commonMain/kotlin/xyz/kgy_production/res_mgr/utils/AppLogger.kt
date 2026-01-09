package xyz.kgy_production.res_mgr.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

expect object PlatformLogger {
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null)
}

object AppLogger {
    fun d(tag: String, message: String) = PlatformLogger.log(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = PlatformLogger.log(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String, t: Throwable? = null) = PlatformLogger.log(LogLevel.WARN, tag, message, t)
    fun e(tag: String, message: String, t: Throwable? = null) = PlatformLogger.log(LogLevel.ERROR, tag, message, t)
}

