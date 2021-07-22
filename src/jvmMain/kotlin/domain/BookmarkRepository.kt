package tkngch.bookmarkManager.jvm.domain

import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.BookmarkId
import tkngch.bookmarkManager.common.model.Datetime
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.TagId
import tkngch.bookmarkManager.common.model.TagName
import tkngch.bookmarkManager.common.model.Username
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.common.model.VisitLog

interface BookmarkRepository {
    fun addNewBookmark(user: Username, bookmark: Bookmark)
    fun addNewTag(user: Username, tag: Tag)
    fun addBookmarkVisitLog(user: Username, log: VisitLog)

    fun bookmark(user: Username, id: BookmarkId): Bookmark?
    fun bookmarks(user: Username, tagIds: List<TagId>): List<Bookmark>
    fun tag(user: Username, id: TagId): Tag
    fun tags(user: Username): List<Tag>

    fun addTagsToBookmark(
        user: Username,
        bookmarkId: BookmarkId,
        tagIds: List<TagId>,
        createdAt: Datetime
    )
    fun dropTagsFromBookmark(user: Username, bookmarkId: BookmarkId, tagIds: List<TagId>)

    fun updateTag(
        user: Username,
        tagId: TagId,
        updatedTagName: TagName,
        updatedVisibility: Visibility
    )

    fun deleteBookmark(user: Username, id: BookmarkId)
    fun deleteTag(user: Username, id: TagId)

    fun visitLogs(user: Username): List<VisitLog>
    fun addOrUpdateScores(scores: List<BookmarkScore>)
}
