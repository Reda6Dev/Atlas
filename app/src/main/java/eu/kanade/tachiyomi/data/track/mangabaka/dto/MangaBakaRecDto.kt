package eu.kanade.tachiyomi.data.track.mangabaka.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MangaBakaRecResult(
    val data: List<MangaBakaRecEntry> = emptyList(),
)

@Serializable
data class MangaBakaRecEntry(
    val series: MangaBakaRecSeries,
    @SerialName("shared_users")
    val sharedUsers: Int? = null,
)

@Serializable
data class MangaBakaRecSeries(
    val id: Long,
    val title: String,
    @SerialName("native_title")
    val nativeTitle: String? = null,
    @SerialName("romanized_title")
    val romanizedTitle: String? = null,
    val type: String = "manga",
    @SerialName("content_rating")
    val contentRating: String? = null,
    val cover: MangaBakaCover? = null,
)
