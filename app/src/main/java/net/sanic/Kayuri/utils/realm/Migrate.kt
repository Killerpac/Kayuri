package net.sanic.Kayuri.utils.realm

import io.realm.DynamicRealm
import io.realm.RealmMigration

class Migrate : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var oldVersion = oldVersion
        // DynamicRealm exposes an editable schema
        val schema = realm.schema
        if (oldVersion == 0L) {
            schema.get("Content")
                ?.addRealmListField("quality", String::class.java)
                ?.addField("index", Int::class.java)
                ?.removeField("url")
                ?.addRealmListField("url", String::class.java)
            oldVersion++
        }
        if (oldVersion == 1L) {
            schema.create("RecentlyPlayed")
                .addField("ID", Int::class.java)
                .addPrimaryKey("ID")
                .addField("imageUrl", String::class.java)
                .addField("categoryUrl", String::class.java)
                .addField("episodeUrl", String::class.java)
                .addField("title", String::class.java)
                .addField("episodeNumber", String::class.java)
                .addField("timestamp",Long::class.java)
        }
    }
}