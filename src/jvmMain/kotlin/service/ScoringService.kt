package tkngch.bookmarkManager.jvm.service

import tkngch.bookmarkManager.common.model.Username
import tkngch.bookmarkManager.jvm.adapter.BookmarkRepository
import tkngch.bookmarkManager.jvm.domain.BookmarkScore
import tkngch.bookmarkScorer.domain.VisitInstant
import tkngch.bookmarkScorer.domain.Visits
import java.time.Instant

interface ScoringService {

    fun updateScores(user: Username)
}

class ScoringServiceImpl(
    private val bookmarkRepo: BookmarkRepository
) : ScoringService {

    override fun updateScores(user: Username) {
        val visitLogs = bookmarkRepo.visitLogs(user)

        val visits = Visits(
            records =
                visitLogs.map { log ->
                    VisitInstant(
                        bookmarkId = log.bookmarkId,
                        instant = Instant.parse(log.visitedAt)
                    )
                }
        )

        val scores = visits.inferDailyCounts().asIterable().map { entry ->
            BookmarkScore.make(bookmarkId = entry.key, score = entry.value)
        }
        scores.forEach { bookmarkRepo.addOrUpdateScore(it) }
    }
}
