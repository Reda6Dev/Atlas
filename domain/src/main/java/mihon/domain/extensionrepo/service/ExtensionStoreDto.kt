package mihon.domain.extensionrepo.service

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.protobuf.ProtoNumber
import mihon.domain.extensionrepo.model.ExtensionRepo

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ExtensionStoreDto(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val badgeLabel: String,
    @ProtoNumber(3) val signingKey: String,
    @ProtoNumber(4) val contact: Contact,
    @ProtoNumber(101) val extensionList: ExtensionListDto? = null,
    @ProtoNumber(102) val extensionListUrl: String? = null,
) {
    @Serializable
    data class Contact(
        @ProtoNumber(1) val website: String,
        @ProtoNumber(2) val discord: String? = null,
    )

    @Suppress("Unused")
    enum class ContentWarning {
        @ProtoNumber(0) @JsonNames("CONTENT_WARNING_UNSPECIFIED") UNSPECIFIED,
        @ProtoNumber(1) @JsonNames("CONTENT_WARNING_SAFE") SAFE,
        @ProtoNumber(2) @JsonNames("CONTENT_WARNING_MIXED") MIXED,
        @ProtoNumber(3) @JsonNames("CONTENT_WARNING_NSFW") NSFW,
    }
}

@Serializable
data class ExtensionListDto(@ProtoNumber(1) val extensions: List<ExtensionDto>)

@Serializable
data class ExtensionDto(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val packageName: String,
    @ProtoNumber(3) val resources: ResourcesDto,
    @ProtoNumber(4) val extensionLib: String,
    @ProtoNumber(5) val versionCode: Long,
    @ProtoNumber(6) val versionName: String,
    @ProtoNumber(7) val contentWarning: ExtensionStoreDto.ContentWarning,
    @ProtoNumber(8) val sources: List<SourceDto>,
)

@Serializable
data class ResourcesDto(
    @ProtoNumber(1) val apkUrl: String,
    @ProtoNumber(2) val iconUrl: String,
)

@Serializable
data class SourceDto(
    @ProtoNumber(1) val id: Long,
    @ProtoNumber(2) val name: String,
    @ProtoNumber(3) val language: String,
    @ProtoNumber(4) val homeUrl: String = "",
    @ProtoNumber(5) val mirrorUrls: List<String> = emptyList(),
    @ProtoNumber(7) val message: String? = null,
)

fun ExtensionStoreDto.toExtensionRepo(indexUrl: String) = ExtensionRepo(
    baseUrl = indexUrl,
    name = name,
    shortName = badgeLabel,
    website = contact.website,
    signingKeyFingerprint = signingKey,
)
