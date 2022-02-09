package net.sanic.Kayuri.utils.realm

import io.realm.*

class Migrate : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var oldVersion = oldVersion
        // DynamicRealm exposes an editable schema
        val schema = realm.schema
        if (oldVersion == 0L) {
            schema.get("Content")
                ?.addRealmListField("quality", String::class.java)
                ?.addField("index",Int::class.java)
                ?.removeField("url")
                ?.addRealmListField("url", String::class.java)
            oldVersion++
        }
    }
}