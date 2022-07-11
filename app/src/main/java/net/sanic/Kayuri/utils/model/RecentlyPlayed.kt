package net.sanic.Kayuri.utils.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RecentlyPlayed (
    @PrimaryKey
    var ID: Int = 0,
    var imageUrl: String? = "",
    var categoryUrl: String? = null,
    var episodeUrl: String? = null,
    var title: String? = "",
    var episodeNumber: String? = null,
    var timestamp: Long = System.currentTimeMillis()
):RealmObject()