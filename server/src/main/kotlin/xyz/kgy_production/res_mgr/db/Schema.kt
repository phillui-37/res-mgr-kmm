package xyz.kgy_production.res_mgr.db

import org.jetbrains.exposed.sql.Table

object Locations : Table("location") {
    val id = uuid("id")
    val name = varchar("name", 200)
    val path = text("path")
    val clientType = varchar("client_type", 200)

    override val primaryKey = PrimaryKey(id, name, clientType)
}

object Tags : Table("tag") {
    val id = uuid("id")
    val name = varchar("name", 200)

    override val primaryKey = PrimaryKey(id, name)
}

object Categories : Table("category") {
    val id = uuid("id")
    val name = varchar("name", 200)

    override val primaryKey = PrimaryKey(id, name)
}

object Items : Table("item") {
    val id = uuid("id")
    val name = varchar("name", 500).default("")
    val categoryId = uuid("category_id").references(Categories.id)
    val isSafe = bool("is_safe")

    override val primaryKey = PrimaryKey(id)
}

object Props : Table("prop") {
    val id = uuid("id")
    val name = varchar("name", 200)
    val type = varchar("type", 50)

    override val primaryKey = PrimaryKey(id)
}

// M-M Tables

object ItemTags : Table("item_tag_map") {
    val itemId = uuid("item_id").references(Items.id)
    val tagId = uuid("tag_id").references(Tags.id)

    override val primaryKey = PrimaryKey(itemId, tagId)
}

object ItemLocations : Table("item_location_map") {
    val itemId = uuid("item_id").references(Items.id)
    val locationId = uuid("location_id").references(Locations.id)

    override val primaryKey = PrimaryKey(itemId, locationId)
}

object CategoryProps : Table("category_prop_map") {
    val categoryId = uuid("category_id").references(Categories.id)
    val propId = uuid("prop_id").references(Props.id)

    override val primaryKey = PrimaryKey(categoryId, propId)
}

object ItemPropValues : Table("item_prop_value") {
    val itemId = uuid("item_id").references(Items.id)
    val propId = uuid("prop_id").references(Props.id)
    val value = text("value")

    override val primaryKey = PrimaryKey(itemId, propId)
}
