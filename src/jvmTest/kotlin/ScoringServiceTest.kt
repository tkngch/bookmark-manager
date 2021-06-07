package tkngcg.bookmarkManager.jvm.service

import org.junit.jupiter.api.Test
import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.BookmarkId
import tkngch.bookmarkManager.common.model.Datetime
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.TagId
import tkngch.bookmarkManager.common.model.TagName
import tkngch.bookmarkManager.common.model.Username
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.common.model.VisitLog
import tkngch.bookmarkManager.jvm.domain.BookmarkRepository
import tkngch.bookmarkManager.jvm.domain.BookmarkScore
import tkngch.bookmarkManager.jvm.service.ScoringServiceImpl
import java.time.Instant
import kotlin.test.assertEquals

class ScoringServiceTest {

    class MockBookmarkRepository : BookmarkRepository {
        override fun addNewBookmark(user: Username, bookmark: Bookmark) =
            TODO("Not yet implemented")

        override fun addNewTag(user: Username, tag: Tag) =
            TODO("Not yet implemented")

        override fun addBookmarkVisitLog(user: Username, log: VisitLog) =
            TODO("Not yet implemented")

        override fun bookmark(user: Username, id: BookmarkId): Bookmark? =
            TODO("Not yet implemented")

        override fun bookmarks(user: Username, tagIds: List<TagId>): List<Bookmark> =
            TODO("Not yet implemented")

        override fun tag(user: Username, id: TagId): Tag =
            TODO("Not yet implemented")

        override fun tags(user: Username): List<Tag> =
            TODO("Not yet implemented")

        override fun addTagsToBookmark(
            user: Username,
            bookmarkId: BookmarkId,
            tagIds: List<TagId>,
            createdAt: Datetime
        ) =
            TODO("Not yet implemented")

        override fun dropTagsFromBookmark(
            user: Username,
            bookmarkId: BookmarkId,
            tagIds: List<TagId>
        ) =
            TODO("Not yet implemented")

        override fun updateTag(
            user: Username,
            tagId: TagId,
            updatedTagName: TagName,
            updatedVisibility: Visibility
        ) =
            TODO("Not yet implemented")

        override fun deleteBookmark(user: Username, id: BookmarkId) =
            TODO("Not yet implemented")

        override fun deleteTag(user: Username, id: TagId) =
            TODO("Not yet implemented")

        override fun visitLogs(user: Username) = listOf(
            VisitLog(bookmarkId = "bookmarkId 01", visitedAt = Instant.now().toString()),
            VisitLog(bookmarkId = "bookmarkId 02", visitedAt = Instant.now().toString()),
            VisitLog(bookmarkId = "bookmarkId 02", visitedAt = Instant.now().toString()),
            VisitLog(bookmarkId = "bookmarkId 03", visitedAt = Instant.now().toString()),
            VisitLog(bookmarkId = "bookmarkId 03", visitedAt = Instant.now().toString()),
            VisitLog(bookmarkId = "bookmarkId 03", visitedAt = Instant.now().toString()),
        )

        var scores: MutableList<BookmarkScore> = mutableListOf()
        override fun addOrUpdateScore(score: BookmarkScore) {
            this.scores.add(score)
        }
    }

    @Test
    fun `test updating scores`() {
        val bookmarkRepository = MockBookmarkRepository()
        val instance = ScoringServiceImpl(bookmarkRepository)
        val username = "user"
        instance.updateScores(username)

        val nBookmarkIds =
            bookmarkRepository.visitLogs(username).map { it.bookmarkId }.distinct().size
        assertEquals(nBookmarkIds, bookmarkRepository.scores.size)
    }
}
