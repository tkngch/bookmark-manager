package tkngch.bookmarkManager.common.model

import kotlinx.serialization.Serializable

typealias BookmarkId = String
typealias TagId = String
typealias URL = String
typealias TagName = String
typealias Datetime = String
typealias Username = String

enum class Visibility { PRIMARY, SECONDARY; }

@Serializable
data class Bookmark(
    val id: BookmarkId,
    val title: String,
    val url: URL,
    @Serializable // Unless explicitly annotated, a list is serialized as String, not Json array.
    val tags: List<Tag>,
    val createdAt: Datetime,
)

@Serializable
data class Tag(
    val id: TagId,
    val name: TagName,
    val visibility: Visibility,
    val createdAt: Datetime
)

@Serializable
data class VisitLog(
    val bookmarkId: BookmarkId,
    val visitedAt: Datetime
)
