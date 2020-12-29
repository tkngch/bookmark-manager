package tkngch.bookmarkManager.jvm.service

import tkngch.bookmarkManager.common.model.Username
import tkngch.bookmarkManager.jvm.adapter.BookmarkRepository
import tkngch.bookmarkManager.jvm.domain.BookmarkScore
import tkngch.bookmarkScorer.domain.Visit
import tkngch.bookmarkScorer.domain.Visits
import java.time.Instant

interface ScoringService {

    fun updateScores(user: Username)
}

class ScoringFrequencyServiceImpl(
    private val bookmarkRepo: BookmarkRepository
) : ScoringService {

    override fun updateScores(user: Username) {
        val visitLogs = bookmarkRepo.visitLogs(user)

        val visits = Visits(
            records =
                visitLogs.groupBy { it.bookmarkId }.asIterable().map { log ->
                    Visit(
                        bookmarkId = log.key,
                        visitDates = log.value.map { Instant.parse(it.visitedAt) }
                    )
                }
        )

        val scores = visits.inferredAverageDailyVisits.map {
            BookmarkScore.make(bookmarkId = it.bookmarkId, score = it.score)
        }
        scores.forEach { bookmarkRepo.addOrUpdateScore(it) }
    }
}
