package tachiyomi.domain.release.model

import android.os.Build

/**
 * Contains information about the latest release.
 */
data class Release(
    val version: String,
    val info: String,
    val releaseLink: String,
    private val assets: List<String>,
) {

    /**
     * Get download link of latest release from the assets.
     * @return download link of latest release, or empty string if no valid APK is found.
     */
    fun getDownloadLink(): String {
        val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: ""
        val apkVariant = when (primaryAbi) {
            "arm64-v8a" -> "-arm64-v8a"
            "armeabi-v7a" -> "-armeabi-v7a"
            "x86" -> "-x86"
            "x86_64" -> "-x86_64"
            else -> ""
        }

        val apkAssets = assets.filter { it.endsWith(".apk", ignoreCase = true) }

        // 1. Try exact ABI match for Atlas or TachiyomiSY
        if (apkVariant.isNotEmpty()) {
            val abiMatch = apkAssets.find {
                it.contains("Atlas$apkVariant", ignoreCase = true) ||
                    it.contains("TachiyomiSY$apkVariant", ignoreCase = true)
            }
            if (abiMatch != null) return abiMatch
        }

        // 2. Try explicit Universal APK or exact name "Atlas.apk" / "TachiyomiSY.apk"
        val universalMatch = apkAssets.find {
            it.contains("Atlas-universal", ignoreCase = true) ||
                it.contains("TachiyomiSY-universal", ignoreCase = true) ||
                it.endsWith("/Atlas.apk", ignoreCase = true) ||
                it.endsWith("/TachiyomiSY.apk", ignoreCase = true)
        }
        if (universalMatch != null) return universalMatch

        // 3. Fallback to any APK that does not explicitly target a conflicting ABI
        val allAbis = listOf("-arm64-v8a", "-armeabi-v7a", "-x86_64", "-x86")
        val conflictingAbis = allAbis.filter { it != apkVariant }

        val safeFallback = apkAssets.find { asset ->
            conflictingAbis.none { asset.contains(it, ignoreCase = true) }
        }

        return safeFallback ?: ""
    }

    /**
     * Assets class containing download url.
     */
    data class Assets(val downloadLink: String)
}
