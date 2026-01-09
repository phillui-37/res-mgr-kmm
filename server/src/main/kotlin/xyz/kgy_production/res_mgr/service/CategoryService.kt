package xyz.kgy_production.res_mgr.service

import xyz.kgy_production.res_mgr.model.CategoryDto

interface CategoryService {
    suspend fun getCategories(): Result<List<CategoryDto>>
    suspend fun createCategory(name: String): Result<CategoryDto>
    suspend fun deleteCategory(id: String): Result<Boolean>
}

