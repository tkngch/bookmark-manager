package tkngch.bookmarkManager.common.model

import kotlinx.serialization.Serializable

@Serializable
data class PayloadTagCreate(
    val tagName: TagName,
    val visibility: Visibility
)

@Serializable
data class PayloadTagUpdate(
    val tagId: TagId,
    val updatedTagName: TagName,
    val updatedVisibility: Visibility
)

@Serializable
data class PayloadTagDelete(val tagId: TagId)

@Serializable
data class PayloadBookmarkCreate(
    val url: URL,
    @Serializable
    val tags: List<Tag>
)

@Serializable
data class PayloadBookmarkUpdateTags(
    val bookmarkId: BookmarkId,
    val tags: List<TagId>
)

@Serializable
data class PayloadBookmarkRefresh(val bookmarkId: BookmarkId)

@Serializable
data class PayloadBookmarkDelete(val bookmarkId: BookmarkId)

@Serializable
data class PayloadBookmarkVisit(val bookmarkId: BookmarkId)
