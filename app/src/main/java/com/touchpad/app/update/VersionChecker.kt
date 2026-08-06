package com.touchpad.app.update

import com.touchpad.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class RemoteVersion(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val critical: Boolean = false,
)

/**
 * Fetches the static `version.json` manifest over HTTPS.
 *
 * Why not GitHub REST API: rate limits and PAT exposure. A static file on Pages
 * (or similar) keeps update checks anonymous and cache-friendly.
 */
class VersionChecker(
    private val manifestUrl: String = BuildConfig.VERSION_MANIFEST_URL,
) {
    suspend fun checkForUpdate(): RemoteVersion? = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(manifestUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5_000
                readTimeout = 5_000
                requestMethod = "GET"
            }
            connection.inputStream.bufferedReader().use { reader ->
                val json = JSONObject(reader.readText())
                val remote = RemoteVersion(
                    versionCode = json.getInt("versionCode"),
                    versionName = json.getString("versionName"),
                    apkUrl = json.getString("apkUrl"),
                    critical = json.optBoolean("critical", false),
                )
                if (remote.versionCode > BuildConfig.VERSION_CODE) remote else null
            }
        } catch (_: Exception) {
            // Offline / missing Pages host is non-fatal for a local toy.
            null
        }
    }
}
