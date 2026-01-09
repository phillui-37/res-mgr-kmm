package xyz.kgy_production.res_mgr.di

import xyz.kgy_production.res_mgr.config.AppConfig
import xyz.kgy_production.res_mgr.config.JvmAppConfig

actual fun getPlatformConfig(): AppConfig = JvmAppConfig()

