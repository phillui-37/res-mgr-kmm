package xyz.kgy_production.res_mgr.di

import kotlinx.coroutines.asCoroutineDispatcher
import org.koin.core.qualifier.named
import org.koin.dsl.module
import xyz.kgy_production.res_mgr.service.CategoryService
import xyz.kgy_production.res_mgr.service.ItemService
import xyz.kgy_production.res_mgr.service.impl.CategoryServiceImpl
import xyz.kgy_production.res_mgr.service.impl.ItemServiceImpl
import java.util.concurrent.Executors

val serverModule = module {
    single(named("IODispatcher")) {
        val cpuCount = Runtime.getRuntime().availableProcessors()
        Executors.newFixedThreadPool(cpuCount * 2).asCoroutineDispatcher()
    }

    single<ItemService> { ItemServiceImpl(get(named("IODispatcher"))) }
    single<CategoryService> { CategoryServiceImpl(get(named("IODispatcher"))) }
}
