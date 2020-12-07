package tkngch.bookmarkManager.jvm.service

import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.BookmarkId
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.TagId
import tkngch.bookmarkManager.common.model.TagName
import tkngch.bookmarkManager.common.model.URL
import tkngch.bookmarkManager.common.model.Username
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.jvm.adapter.BookmarkRepository
import tkngch.bookmarkManager.jvm.adapter.WebScraping
import tkngch.bookmarkManager.jvm.domain.BookmarkDomain

interface BookmarkService {

    fun createBookmark(user: Username, url: URL, tags: List<Tag>)
    fun createTag(user: Username, tagName: TagName, visibility: Visibility)

    fun getBookmarks(user: Username, tagIds: List<TagId>? = null): List<Bookmark>
    fun getTags(user: Username): List<Tag>

    fun addTagsToBookmark(user: Username, bookmarkId: BookmarkId, tagIds: List<TagId>)
    fun dropTagsFromBookmark(user: Username, bookmarkId: BookmarkId, tagIds: List<TagId>)
    fun updateTag(
        user: Username,
        tagId: TagId,
        updatedTagName: TagName,
        updatedVisibility: Visibility
    )

    fun deleteBookmark(user: Username, bookmarkId: BookmarkId)
    fun deleteTag(user: Username, tagId: TagId)

    fun logBookmarkVisit(user: Username, bookmarkId: BookmarkId)
}

class BookmarkServiceImpl(
    private val bookmarkRepo: BookmarkRepository,
    private val bookmarkDom: BookmarkDomain,
    private val webScraper: WebScraping
) : BookmarkService {

    override fun createBookmark(user: Username, url: URL, tags: List<Tag>) {
        val title = webScraper.title(url)
        val bookmark = bookmarkDom.makeNewBookmark(title, url, tags)
        bookmarkRepo.addNewBookmark(user, bookmark)
    }

    override fun createTag(user: Username, tagName: TagName, visibility: Visibility) {
        val tag = bookmarkDom.makeNewTag(tagName, visibility)
        bookmarkRepo.addNewTag(user, tag)
    }

    override fun getBookmarks(user: Username, tagIds: List<TagId>?): List<Bookmark> =
        bookmarkRepo.bookmarks(user, tagIds ?: listOf())

    override fun getTags(user: Username): List<Tag> = bookmarkRepo.tags(user)

    override fun addTagsToBookmark(
        user: Username,
        bookmarkId: BookmarkId,
        tagIds: List<TagId>
    ) = bookmarkRepo.addTagsToBookmark(user, bookmarkId, tagIds, bookmarkDom.getCurrentDateTime())

    override fun dropTagsFromBookmark(
        user: Username,
        bookmarkId: BookmarkId,
        tagIds: List<TagId>
    ) = bookmarkRepo.dropTagsFromBookmark(user, bookmarkId, tagIds)

    override fun updateTag(
        user: Username,
        tagId: TagId,
        updatedTagName: TagName,
        updatedVisibility: Visibility
    ) =
        bookmarkRepo.updateTag(user, tagId, updatedTagName, updatedVisibility)

    override fun deleteBookmark(user: Username, bookmarkId: BookmarkId): Unit =
        bookmarkRepo.deleteBookmark(user, bookmarkId)

    override fun deleteTag(user: Username, tagId: TagId): Unit = bookmarkRepo.deleteTag(user, tagId)

    override fun logBookmarkVisit(user: Username, bookmarkId: BookmarkId) {
        val log = bookmarkDom.makeVisitLog(bookmarkId)
        bookmarkRepo.addBookmarkVisitLog(user, log)
        // TODO: train a model in a forked process?
    }
}
