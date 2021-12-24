package net.perfectdreams.galleryofdreams.backend.utils.exposed

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.postgresql.util.PGobject

/**
 * Creates and updates a PostgreSQL enum based on a Kotlin enum
 *
 * **This does not remove values from the PostgreSQL enum, and it does not insert new enums based on order!**
 *
 * @param enumValues a callback that provides a list of the valid enum values
 */
inline fun <reified T : Enum<T>> Transaction.createOrUpdatePostgreSQLEnum(enumValues: Array<T>) {
    val valueNames = enumValues.map { it.name }
    val clazzName = T::class.simpleName!!
    val psqlType = clazzName.lowercase()
    val joined = valueNames.joinToString { "'$it'" }

    val alreadyInsertedEnumValues = mutableSetOf<String>()

    exec("SELECT  n.nspname AS enum_schema,  \n" +
            "        t.typname AS enum_name,  \n" +
            "        e.enumlabel AS enum_value\n" +
            "FROM    pg_type t JOIN \n" +
            "        pg_enum e ON t.oid = e.enumtypid JOIN \n" +
            "        pg_catalog.pg_namespace n ON n.oid = t.typnamespace\n" +
            "WHERE   t.typname = '$psqlType'") {
        while (it.next())
            alreadyInsertedEnumValues.add(it.getString("enum_value"))
    }

    val missingEnums = valueNames.filter { it !in alreadyInsertedEnumValues }

    if (alreadyInsertedEnumValues.isEmpty()) {
        exec("CREATE TYPE ${clazzName.lowercase()} AS ENUM ($joined);")
    } else if (missingEnums.isNotEmpty()) {
        for (missingEnum in missingEnums) {
            exec("ALTER TYPE ${clazzName.lowercase()} ADD VALUE '$missingEnum';")
        }
    }
}

inline fun <reified T : Enum<T>> Table.postgresEnumeration(
    columnName: String
) = customEnumeration(columnName, T::class.simpleName!!.lowercase(),
    { value -> enumValueOf<T>(value as String) }, { PGEnum(T::class.simpleName!!.lowercase(), it) })

// From https://github.com/JetBrains/Exposed/wiki/DataTypes
class PGEnum<T : Enum<T>>(enumTypeName: String, enumValue: T?) : PGobject() {
    init {
        value = enumValue?.name
        type = enumTypeName
    }
}