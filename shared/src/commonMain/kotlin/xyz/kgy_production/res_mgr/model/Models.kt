package xyz.kgy_production.res_mgr.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val id: String,
    val name: String,
    val path: String,
    val clientType: String
)

@Serializable
data class TagDto(
    val id: String,
    val name: String
)

@Serializable
data class CategoryDto(
    val id: String,
    val name: String
)

@Serializable
data class ItemDto(
    val id: String,
    val name: String,
    val categoryId: String,
    val isSafe: Boolean,
    val tags: List<TagDto> = emptyList(),
    val locations: List<LocationDto> = emptyList(),
    val props: Map<String, String> = emptyMap()
)

@Serializable
data class PropDto(
    val id: String,
    val name: String,
    val type: String
)

@Serializable
data class SearchQuery(
    val query: String,
    val limit: Int = 20,
    val offset: Int = 0,
    val filter: Map<String, String> = emptyMap() // e.g. "categoryId" -> "..."
)
