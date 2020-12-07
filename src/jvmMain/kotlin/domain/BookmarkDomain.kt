package tkngch.bookmarkManager.jvm.domain

import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.BookmarkId
import tkngch.bookmarkManager.common.model.Datetime
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.URL
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.common.model.VisitLog
import java.time.Instant
import java.util.UUID

interface BookmarkDomain {
    fun makeNewBookmark(title: String, url: URL, tags: List<Tag>): Bookmark
    fun makeNewTag(name: String, visibility: Visibility): Tag
    fun makeVisitLog(bookmarkId: BookmarkId): VisitLog
    fun getCurrentDateTime(): Datetime
}

class BookmarkDomainImpl : BookmarkDomain {

    override fun makeNewBookmark(title: String, url: URL, tags: List<Tag>) =
        Bookmark(
            id = UUID.randomUUID().toString(),
            title = title,
            url = url,
            tags = tags,
            createdAt = this.getCurrentDateTime()
        )

    override fun makeNewTag(name: String, visibility: Visibility): Tag =
        Tag(
            id = UUID.randomUUID().toString(),
            name = name,
            visibility = visibility,
            createdAt = this.getCurrentDateTime()
        )

    override fun makeVisitLog(bookmarkId: BookmarkId) = VisitLog(
        bookmarkId = bookmarkId,
        visitedAt = this.getCurrentDateTime()
    )

    override fun getCurrentDateTime() = Instant.now().toString()
}
