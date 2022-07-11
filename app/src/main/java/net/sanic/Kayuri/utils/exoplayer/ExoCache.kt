package net.sanic.Kayuri.utils.exoplayer

import android.content.Context
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

class ExoCache {
    companion object {
        private var simpleCache: SimpleCache? = null
        fun getcacheinstance(context: Context): SimpleCache {
            if (simpleCache == null) {
                val leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024)
                val databaseProvider = StandaloneDatabaseProvider(context)
                simpleCache = SimpleCache(
                    File(context.cacheDir, "media").also { it.deleteOnExit() },
                    leastRecentlyUsedCacheEvictor,
                    databaseProvider
                )
            }
            return simpleCache as SimpleCache
        }
    }
}