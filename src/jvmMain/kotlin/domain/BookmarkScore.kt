package tkngch.bookmarkManager.jvm.domain

import tkngch.bookmarkManager.common.model.BookmarkId
import tkngch.bookmarkManager.common.model.Datetime
import java.time.Instant

data class BookmarkScore(val bookmarkId: BookmarkId, val score: Double, val updatedAt: Datetime) {
    companion object {
        fun make(bookmarkId: BookmarkId, score: Double) = BookmarkScore(
            bookmarkId = bookmarkId,
            score = score,
            updatedAt = Instant.now().toString()
        )
    }
}
