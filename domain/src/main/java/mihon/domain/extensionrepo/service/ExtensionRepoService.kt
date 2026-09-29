package mihon.domain.extensionrepo.service

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import logcat.LogPriority
import mihon.domain.extensionrepo.model.ExtensionRepo
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.system.logcat
import java.util.zip.GZIPInputStream

class ExtensionRepoService(
    networkHelper: NetworkHelper,
    private val json: Json,
) {
    val client = networkHelper.client

    suspend fun fetchRepoDetails(
        repo: String,
    ): ExtensionRepo? {
        return withIOContext {
            try {
                if (repo.endsWith(".pb")) {
                    fetchStore(repo).toExtensionRepo(repo)
                } else {
                    with(json) {
                        client.newCall(GET("$repo/repo.json"))
                            .awaitSuccess()
                            .parseAs<ExtensionRepoMetaDto>()
                            .toExtensionRepo(baseUrl = repo)
                    }
                }
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e) { "Failed to fetch repo details" }
                null
            }
        }
    }

    suspend fun fetchStore(indexUrl: String): ExtensionStoreDto = withIOContext {
        val bytes = client.newCall(GET(indexUrl)).awaitSuccess().use { it.body.bytes() }
        decode<ExtensionStoreDto>(bytes)
    }

    suspend fun fetchExtensionList(url: String): ExtensionListDto = withIOContext {
        val bytes = client.newCall(GET(url)).awaitSuccess().use { it.body.bytes() }
        decode<ExtensionListDto>(bytes)
    }

    private inline fun <reified T> decode(raw: ByteArray): T {
        val bytes = if (raw.size >= 2 && raw[0] == 0x1f.toByte() && raw[1] == 0x8b.toByte()) {
            GZIPInputStream(raw.inputStream()).use { it.readBytes() }
        } else {
            raw
        }
        // 0x7B = "{" -> JSON, anything else -> protobuf
        return if (bytes.firstOrNull() == 0x7B.toByte()) {
            json.decodeFromString<T>(bytes.decodeToString())
        } else {
            ProtoBuf.decodeFromByteArray<T>(bytes)
        }
    }
}
