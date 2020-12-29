package tkngch.bookmarkManager.jvm.domain

import tkngch.bookmarkManager.common.model.BookmarkId
import tkngch.bookmarkManager.common.model.VisitLog
import java.time.Instant

fun VisitLog.Companion.make(bookmarkId: BookmarkId) =
    VisitLog(
        bookmarkId = bookmarkId,
        visitedAt = Instant.now().toString()
    )
