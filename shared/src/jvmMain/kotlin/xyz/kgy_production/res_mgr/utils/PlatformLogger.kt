package xyz.kgy_production.res_mgr.utils

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual object PlatformLogger {
    private val logDir = File("logs").apply { mkdirs() }
    // Simple rotation by date
    private fun getLogFile(): File {
        val date = java.time.LocalDate.now().toString()
        return File(logDir, "app-$date.log")
    }

    actual fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val now = LocalDateTime.now()
        val crash = throwable?.stackTraceToString() ?: ""
        val line = "$now [${level.name}] [$tag] $message $crash"

        // Console
        println(line)

        // File
        try {
            PrintWriter(FileWriter(getLogFile(), true)).use {
                it.println(line)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

