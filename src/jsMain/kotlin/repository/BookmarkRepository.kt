package tkngch.bookmarkManager.js.repository

import kotlinx.browser.window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.BookmarkId
import tkngch.bookmarkManager.common.model.PayloadBookmarkCreate
import tkngch.bookmarkManager.common.model.PayloadBookmarkDelete
import tkngch.bookmarkManager.common.model.PayloadBookmarkRefresh
import tkngch.bookmarkManager.common.model.PayloadBookmarkUpdateTags
import tkngch.bookmarkManager.common.model.PayloadBookmarkVisit
import tkngch.bookmarkManager.common.model.PayloadTagCreate
import tkngch.bookmarkManager.common.model.PayloadTagDelete
import tkngch.bookmarkManager.common.model.PayloadTagUpdate
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.TagId
import tkngch.bookmarkManager.common.model.TagName
import tkngch.bookmarkManager.common.model.URL
import tkngch.bookmarkManager.common.model.Visibility
import kotlin.js.Promise
import kotlin.js.json

interface BookmarkRepository {
    fun getTags(): Promise<List<Tag>>
    fun createTag(tagName: TagName, visibility: Visibility): Promise<Unit>
    fun updateTag(
        tagId: TagId,
        updatedTagName: TagName,
        updatedVisibility: Visibility
    ): Promise<Unit>
    fun deleteTag(tagId: TagId): Promise<Unit>

    fun getBookmarks(tagIds: List<TagId>): Promise<List<Bookmark>>
    fun createBookmark(url: URL, tags: List<Tag>): Promise<Unit>
    fun refreshBookmark(bookmarkId: BookmarkId): Promise<Unit>
    fun addTagsToBookmark(
        bookmarkId: BookmarkId,
        tags: List<TagId>
    ): Promise<Unit>
    fun dropTagsFromBookmark(
        bookmarkId: BookmarkId,
        tags: List<TagId>
    ): Promise<Unit>
    fun deleteBookmark(bookmarkId: BookmarkId): Promise<Unit>

    fun logBookmarkVisit(bookmarkId: BookmarkId): Promise<Unit>
}

class BookmarkRepositoryImpl : BookmarkRepository {
    private val endpoint = window.location.origin
    private val header = json("Content-Type" to "application/json")

    override fun getTags() =
        window.fetch(
            "${this.endpoint}/api/tag",
            init = RequestInit(method = "GET", headers = header)
        ).then { it.text() }.then { Json.decodeFromString<List<Tag>>(it) }

    override fun createTag(tagName: TagName, visibility: Visibility) =
        window.fetch(
            "${this.endpoint}/api/tag",
            init = RequestInit(
                method = "POST",
                headers = header,
                body = Json.encodeToString(
                    PayloadTagCreate(
                        tagName = tagName,
                        visibility = visibility
                    )
                ),
            )
        ).then {}

    override fun updateTag(
        tagId: TagId,
        updatedTagName: TagName,
        updatedVisibility: Visibility
    ) =
        window.fetch(
            "${this.endpoint}/api/tag",
            init = RequestInit(
                method = "PUT",
                headers = header,
                body = Json.encodeToString(
                    PayloadTagUpdate(
                        tagId = tagId,
                        updatedTagName = updatedTagName,
                        updatedVisibility = updatedVisibility
                    )
                ),
            )
        ).then {}

    override fun deleteTag(tagId: TagId) =
        window.fetch(
            "${this.endpoint}/api/tag",
            init = RequestInit(
                method = "DELETE",
                headers = header,
                body = Json.encodeToString(PayloadTagDelete(tagId = tagId)),
            )
        ).then {}

    override fun getBookmarks(tagIds: List<TagId>) =
        window.fetch(
            "${this.endpoint}/api/bookmark?${getQueryTags(tagIds)}",
            init = RequestInit(
                method = "GET",
                headers = header
            )
        ).then { it.text() }.then { Json.decodeFromString<List<Bookmark>>(it) }

    private fun getQueryTags(tagIds: List<TagId>): String =
        if (tagIds.isEmpty()) "" else tagIds.map { "tag=$it" }.reduce { a, b -> "$a&$b" }

    override fun createBookmark(url: URL, tags: List<Tag>) =
        window.fetch(
            "${this.endpoint}/api/bookmark",
            init = RequestInit(
                method = "POST",
                headers = header,
                body = Json.encodeToString(
                    PayloadBookmarkCreate(url = url, tags = tags)
                ),
            )
        ).then {}

    override fun refreshBookmark(bookmarkId: BookmarkId) =
        window.fetch(
            "${this.endpoint}/api/bookmark",
            init = RequestInit(
                method = "PUT",
                headers = header,
                body = Json.encodeToString(
                    PayloadBookmarkRefresh(bookmarkId = bookmarkId)
                ),
            )
        ).then {}

    override fun addTagsToBookmark(bookmarkId: BookmarkId, tags: List<TagId>) =
        window.fetch(
            "${this.endpoint}/api/bookmark/tag",
            init = RequestInit(
                method = "POST",
                headers = header,
                body = Json.encodeToString(
                    PayloadBookmarkUpdateTags(
                        bookmarkId = bookmarkId,
                        tags = tags
                    )
                ),
            )
        ).then {}

    override fun dropTagsFromBookmark(
        bookmarkId: BookmarkId,
        tags: List<TagId>
    ) =
        window.fetch(
            "${this.endpoint}/api/bookmark/tag",
            init = RequestInit(
                method = "DELETE",
                headers = header,
                body = Json.encodeToString(
                    PayloadBookmarkUpdateTags(
                        bookmarkId = bookmarkId,
                        tags = tags
                    )
                ),
            )
        ).then {}

    override fun deleteBookmark(bookmarkId: BookmarkId) =
        window.fetch(
            "${this.endpoint}/api/bookmark",
            init = RequestInit(
                method = "DELETE",
                headers = header,
                body = Json.encodeToString(
                    PayloadBookmarkDelete(bookmarkId = bookmarkId)
                ),
            )
        ).then {}

    override fun logBookmarkVisit(bookmarkId: BookmarkId) =
        window.fetch(
            "${this.endpoint}/api/bookmark/visit",
            init = RequestInit(
                method = "POST",
                headers = header,
                body = Json.encodeToString(
                    PayloadBookmarkVisit(bookmarkId = bookmarkId)
                ),
            )
        ).then {}
}
