package tkngcg.bookmarkManager.jvm.service

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import tkngch.bookmarkManager.common.model.VisitLog
import tkngch.bookmarkManager.jvm.domain.BookmarkRepository
import tkngch.bookmarkManager.jvm.service.ScoringServiceImpl
import java.time.Instant

class ScoringServiceTest {

    @Test
    fun `test updating scores`() {
        val bookmarks = listOf(
            VisitLog(bookmarkId = "bookmarkId 01", visitedAt = Instant.now().toString()),
            VisitLog(bookmarkId = "bookmarkId 02", visitedAt = Instant.now().toString()),
            VisitLog(bookmarkId = "bookmarkId 02", visitedAt = Instant.now().toString()),
            VisitLog(bookmarkId = "bookmarkId 03", visitedAt = Instant.now().toString()),
            VisitLog(bookmarkId = "bookmarkId 03", visitedAt = Instant.now().toString()),
            VisitLog(bookmarkId = "bookmarkId 03", visitedAt = Instant.now().toString()),
        )
        val username = "user"
        val nBookmarkIds = bookmarks.map { it.bookmarkId }.distinct().size

        val bookmarkRepository = mockk<BookmarkRepository>()
        every { bookmarkRepository.visitLogs(username) } returns bookmarks
        every { bookmarkRepository.addOrUpdateScores(allAny()) } returns Unit

        val instance = ScoringServiceImpl(bookmarkRepository)
        instance.updateScores(username)

        verify(exactly = 1) { bookmarkRepository.visitLogs(username) }
        verify(exactly = 1) {
            bookmarkRepository.addOrUpdateScores(match { it.size == nBookmarkIds })
        }

        confirmVerified(bookmarkRepository)
    }
}
