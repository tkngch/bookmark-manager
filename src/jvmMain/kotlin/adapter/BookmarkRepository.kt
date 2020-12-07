package tkngch.bookmarkManager.jvm.adapter

import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.BookmarkId
import tkngch.bookmarkManager.common.model.Datetime
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.TagId
import tkngch.bookmarkManager.common.model.TagName
import tkngch.bookmarkManager.common.model.Username
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.common.model.VisitLog
import tkngch.bookmarkManager.jvm.database.Database

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
}

class BookmarkRepositoryImpl(driver: JdbcSqliteDriver) : BookmarkRepository {

    private val database = Database(driver)

    init {
        val cursor: SqlCursor = driver.executeQuery(
            identifier = null,
            sql = "PRAGMA user_version;",
            parameters = 0
        )
        val defaultVersion: Long = 0
        val schemaVersion: Long = cursor.getLong(index = 0) ?: defaultVersion
        if (schemaVersion < 1) {
            Database.Schema.create(driver)
            driver.execute(identifier = null, sql = "PRAGMA user_version = 1", parameters = 0)
        }
    }
    private val separator = ";"

    override fun addNewBookmark(user: Username, bookmark: Bookmark) {
        database.bookmarkQueries.insertNewBookmark(
            bookmarkId = bookmark.id,
            username = user,
            url = bookmark.url,
            title = bookmark.title,
            createdAt = bookmark.createdAt
        )

        // When the url is already in the database, `bookmark.id` does not exist
        // in the database.
        bookmark.tags.forEach { tag ->
            database.bookmarkQueries.addTagToBookmarkByURL(
                username = user,
                url = bookmark.url,
                tagId = tag.id,
                createdAt = bookmark.createdAt
            )
        }
    }

    override fun addNewTag(user: Username, tag: Tag) =
        database.bookmarkQueries.insertNewTag(
            tagId = tag.id,
            username = user,
            tagName = tag.name,
            visibility = tag.visibility.toString(),
            createdAt = tag.createdAt
        )

    override fun addBookmarkVisitLog(user: Username, log: VisitLog) =
        database.bookmarkQueries.insertNewLog(
            username = user,
            bookmarkId = log.bookmarkId,
            visitedAt = log.visitedAt
        )

    override fun bookmark(user: Username, id: BookmarkId): Bookmark? =
        database.bookmarkQueries.selectBookmarkById(
            username = user,
            bookmarkId = id,
            sep = this.separator
        ).executeAsList().map {
            Bookmark(
                id = it.bookmarkId,
                title = it.title,
                url = it.url,
                tags = this.decodeTagsFromJsonString(it.tags),
                createdAt = it.createdAt
            )
        }.firstOrNull()

    override fun bookmarks(user: Username, tagIds: List<TagId>): List<Bookmark> =
        if (tagIds.isEmpty()) this.selectBookmarksWithoutTags(user)
        else this.selectBookmarksByTags(user, tagIds)

    private fun selectBookmarksWithoutTags(user: Username): List<Bookmark> =
        database.bookmarkQueries.selectBookmarksWithoutTags(username = user).executeAsList().map {
            Bookmark(
                id = it.bookmarkId,
                title = it.title,
                url = it.url,
                tags = emptyList(),
                createdAt = it.createdAt
            )
        }

    private fun selectBookmarksByTags(user: Username, tagIds: List<TagId>): List<Bookmark> =
        database.bookmarkQueries.selectBookmarksByTags(
            username = user,
            sep = this.separator,
            tagIds = tagIds,
            requiredNTagMatches = tagIds.size.toLong()
        ).executeAsList().map {
            Bookmark(
                id = it.bookmarkId,
                title = it.title,
                url = it.url,
                tags = this.decodeTagsFromJsonString(it.tags),
                createdAt = it.createdAt
            )
        }

    override fun tag(user: Username, id: TagId): Tag = this.selectTags(user, id).first()

    override fun tags(user: Username): List<Tag> = this.selectTags(user, null)

    private fun selectTags(user: Username, tagId: TagId?): List<Tag> =
        database.bookmarkQueries.selectTags(username = user, tagId = tagId).executeAsList().map {
            Tag(
                id = it.id,
                name = it.name,
                visibility = Visibility.valueOf(it.visibility),
                createdAt = it.createdAt
            )
        }

    private fun decodeTagsFromJsonString(jsonString: String): List<Tag> {
        val unsorted: List<Tag> = if (jsonString.isEmpty()) emptyList() else jsonString.split(
            this.separator
        ).map { Json.decodeFromString(it) }

        return listOf(
            unsorted.filter { it.visibility == Visibility.PRIMARY }.sortedBy { it.name },
            unsorted.filter { it.visibility == Visibility.SECONDARY }.sortedBy { it.name }
        ).flatten()
    }

    override fun addTagsToBookmark(
        user: Username,
        bookmarkId: BookmarkId,
        tagIds: List<TagId>,
        createdAt: Datetime
    ): Unit =
        tagIds.forEach { tagId ->
            database.bookmarkQueries.addTagToBookmark(
                bookmarkId = bookmarkId,
                username = user,
                tagId = tagId,
                createdAt = createdAt
            )
        }

    override fun dropTagsFromBookmark(
        user: Username,
        bookmarkId: BookmarkId,
        tagIds: List<TagId>
    ): Unit =
        tagIds.forEach { tagId ->
            database.bookmarkQueries.dropTagFromBookmark(
                username = user,
                bookmarkId = bookmarkId,
                tagId = tagId
            )
        }

    override fun updateTag(
        user: Username,
        tagId: TagId,
        updatedTagName: TagName,
        updatedVisibility: Visibility
    ) =
        database.bookmarkQueries.updateTag(
            username = user,
            id = tagId,
            name = updatedTagName,
            visibility = updatedVisibility.toString()
        )

    override fun deleteBookmark(user: Username, id: BookmarkId): Unit =
        database.bookmarkQueries.deleteBookmark(username = user, bookmarkId = id)

    override fun deleteTag(user: Username, id: TagId): Unit =
        database.bookmarkQueries.deleteTag(username = user, tagId = id)
}
