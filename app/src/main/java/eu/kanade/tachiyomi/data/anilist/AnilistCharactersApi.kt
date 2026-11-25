package eu.kanade.tachiyomi.data.anilist

import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import tachiyomi.core.common.util.lang.withIOContext
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.concurrent.ConcurrentHashMap

data class AnilistCharacter(
    val id: Long,
    val nameFull: String,
    val nameNative: String?,
    val imageUrl: String?,
    val favouritesCount: Long,
    val description: String?,
    val role: String, // "MAIN" or "SUPPORTING"
)

data class AnilistStaff(
    val id: Long,
    val nameFull: String,
    val nameNative: String?,
    val imageUrl: String?,
    val role: String, // e.g., "Story", "Art", "Original Story"
)

data class AnilistMediaDetails(
    val id: Long,
    val titleRomaji: String?,
    val titleEnglish: String?,
    val titleNative: String?,
    val mainCharacters: List<AnilistCharacter>,
    val supportingCharacters: List<AnilistCharacter>,
    val staff: List<AnilistStaff>,
)

data class AnilistSearchResultItem(
    val id: Long,
    val titleRomaji: String?,
    val titleEnglish: String?,
    val titleNative: String?,
    val coverImage: String?,
    val format: String?,
    val year: Int?,
)

class AnilistRateLimitException : Exception("تم تجاوز حد الطلبات المسموح به (429). الرجاء المحاولة بعد قليل.")
class AnilistNetworkException(message: String) : Exception(message)

object AnilistCharactersApi {

    private const val ENDPOINT = "https://graphql.anilist.co/"
    private val json = Json { ignoreUnknownKeys = true }
    private val cache = ConcurrentHashMap<Long, AnilistMediaDetails>()

    fun getCached(mediaId: Long): AnilistMediaDetails? = cache[mediaId]

    suspend fun getMediaDetails(mediaId: Long): AnilistMediaDetails = withIOContext {
        cache[mediaId]?.let { return@withIOContext it }

        val query = """
            query (${'$'}id: Int) {
              Media(id: ${'$'}id, type: MANGA) {
                id
                title { romaji english native }
                characters {
                  edges {
                    role
                    node {
                      id
                      name { full native }
                      image { large }
                      favourites
                      description
                    }
                  }
                }
                staff {
                  edges {
                    role
                    node {
                      id
                      name { full native }
                      image { large }
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val payload = buildJsonObject {
            put("query", query)
            put("variables", buildJsonObject { put("id", mediaId) })
        }

        val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val client = Injekt.get<NetworkHelper>().client

        val response = try {
            client.newCall(POST(ENDPOINT, body = requestBody)).awaitSuccess()
        } catch (e: Exception) {
            if (e.message?.contains("429") == true) {
                throw AnilistRateLimitException()
            }
            throw AnilistNetworkException(e.message ?: "فشل الاتصال بـ AniList")
        }

        val jsonObject = with(json) { response.parseAs<JsonObject>() }
        val mediaObj = jsonObject["data"]?.jsonObject?.get("Media")?.jsonObject
            ?: throw AnilistNetworkException("لم يتم العثور على العمل")

        val titleObj = mediaObj["title"]?.jsonObject
        val romaji = titleObj?.get("romaji")?.jsonPrimitive?.contentOrNull
        val english = titleObj?.get("english")?.jsonPrimitive?.contentOrNull
        val native = titleObj?.get("native")?.jsonPrimitive?.contentOrNull

        val mainChars = mutableListOf<AnilistCharacter>()
        val suppChars = mutableListOf<AnilistCharacter>()

        val charEdges = mediaObj["characters"]?.jsonObject?.get("edges")?.jsonArray ?: JsonArray(emptyList())
        charEdges.forEach { edgeElement ->
            val edgeObj = edgeElement.jsonObject
            val role = edgeObj["role"]?.jsonPrimitive?.contentOrNull ?: "SUPPORTING"
            val nodeObj = edgeObj["node"]?.jsonObject ?: return@forEach

            val id = nodeObj["id"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: return@forEach
            val nameObj = nodeObj["name"]?.jsonObject
            val full = nameObj?.get("full")?.jsonPrimitive?.contentOrNull ?: "غير معروف"
            val nativeName = nameObj?.get("native")?.jsonPrimitive?.contentOrNull
            val img = nodeObj["image"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull
            val favs = nodeObj["favourites"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L
            val desc = nodeObj["description"]?.jsonPrimitive?.contentOrNull

            val char = AnilistCharacter(
                id = id,
                nameFull = full,
                nameNative = nativeName,
                imageUrl = img,
                favouritesCount = favs,
                description = desc,
                role = role,
            )

            if (role == "MAIN") {
                mainChars.add(char)
            } else {
                suppChars.add(char)
            }
        }

        val staffList = mutableListOf<AnilistStaff>()
        val staffEdges = mediaObj["staff"]?.jsonObject?.get("edges")?.jsonArray ?: JsonArray(emptyList())
        staffEdges.forEach { edgeElement ->
            val edgeObj = edgeElement.jsonObject
            val role = edgeObj["role"]?.jsonPrimitive?.contentOrNull ?: "Staff"
            val nodeObj = edgeObj["node"]?.jsonObject ?: return@forEach

            val id = nodeObj["id"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: return@forEach
            val nameObj = nodeObj["name"]?.jsonObject
            val full = nameObj?.get("full")?.jsonPrimitive?.contentOrNull ?: "غير معروف"
            val nativeName = nameObj?.get("native")?.jsonPrimitive?.contentOrNull
            val img = nodeObj["image"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull

            staffList.add(
                AnilistStaff(
                    id = id,
                    nameFull = full,
                    nameNative = nativeName,
                    imageUrl = img,
                    role = role,
                ),
            )
        }

        val details = AnilistMediaDetails(
            id = mediaId,
            titleRomaji = romaji,
            titleEnglish = english,
            titleNative = native,
            mainCharacters = mainChars,
            supportingCharacters = suppChars,
            staff = staffList,
        )

        cache[mediaId] = details
        details
    }

    suspend fun searchMedia(searchQuery: String): List<AnilistSearchResultItem> = withIOContext {
        if (searchQuery.isBlank()) return@withIOContext emptyList()

        val query = """
            query (${'$'}search: String) {
              Page(perPage: 10) {
                media(search: ${'$'}search, type: MANGA) {
                  id
                  title { romaji english native }
                  coverImage { large }
                  format
                  startDate { year }
                }
              }
            }
        """.trimIndent()

        val payload = buildJsonObject {
            put("query", query)
            put("variables", buildJsonObject { put("search", searchQuery) })
        }

        val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val client = Injekt.get<NetworkHelper>().client

        val response = try {
            client.newCall(POST(ENDPOINT, body = requestBody)).awaitSuccess()
        } catch (e: Exception) {
            if (e.message?.contains("429") == true) {
                throw AnilistRateLimitException()
            }
            return@withIOContext emptyList()
        }

        val jsonObject = with(json) { response.parseAs<JsonObject>() }
        val mediaList = jsonObject["data"]?.jsonObject?.get("Page")?.jsonObject?.get("media")?.jsonArray
            ?: return@withIOContext emptyList()

        mediaList.mapNotNull { itemElement ->
            val mediaObj = itemElement.jsonObject
            val id = mediaObj["id"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: return@mapNotNull null
            val titleObj = mediaObj["title"]?.jsonObject
            val romaji = titleObj?.get("romaji")?.jsonPrimitive?.contentOrNull
            val english = titleObj?.get("english")?.jsonPrimitive?.contentOrNull
            val native = titleObj?.get("native")?.jsonPrimitive?.contentOrNull
            val cover = mediaObj["coverImage"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull
            val format = mediaObj["format"]?.jsonPrimitive?.contentOrNull
            val year = mediaObj["startDate"]?.jsonObject?.get("year")?.jsonPrimitive?.contentOrNull?.toIntOrNull()

            AnilistSearchResultItem(
                id = id,
                titleRomaji = romaji,
                titleEnglish = english,
                titleNative = native,
                coverImage = cover,
                format = format,
                year = year,
            )
        }
    }
}
