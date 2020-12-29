package tkngch.bookmarkManager.jvm.adapter

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.jvm.domain.BookmarkScore
import kotlin.test.assertNotEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookmarkRepositoryTest {

    private val repo: BookmarkRepositoryImpl
    init {
        val driver = JdbcSqliteDriver("jdbc:sqlite::memory:")
        repo = BookmarkRepositoryImpl(driver)
    }
    private val datetime = "2020-10-11T00:00:00Z"

    @Nested
    inner class Bookmarks {
        private val username = "TestBookmarks"

        @Test
        fun `add, fetch and delete bookmarks without tags`() {
            val bookmarks = listOf(
                "b82429e3-a9e7-41a4-9d95-a48f8a58a154",
                "a906a174-6899-40e9-b83d-8ee916628201",
                "ed49acaf-adf6-4dd9-a324-c043ff136440",
                "bec90f7c-c132-45f5-b164-4e1adb8161fc"
            ).map {
                Bookmark(
                    id = it,
                    title = it.split("-").first(),
                    url = it.split("-").last(),
                    tags = emptyList(),
                    createdAt = datetime
                )
            }

            bookmarks.forEach { repo.addNewBookmark(username, it) }
            val afterCreating = repo.bookmarks(username, tagIds = listOf())
            assertTrue(afterCreating.containsAll(bookmarks))

            bookmarks.forEach { repo.deleteBookmark(username, it.id) }
            val afterDeleting = repo.bookmarks(username, tagIds = listOf())
            assertTrue(afterDeleting.intersect(bookmarks).isEmpty())
        }

        @Test
        fun `add a bookmark with primary tags`() {
            val id = "0d51f904-617b-4f29-b7be-a6c2b1c6c765"
            val url = "http://create.bookmark.with.visible.tags"

            val tags = listOf(
                "6776eabb-1513-45b2-a320-5b965d7038bb",
                "b66d6e33-2e09-493a-bfc2-f4f2f4db2178"
            ).map {
                Tag(
                    id = it,
                    name = it.split("-").first(),
                    visibility = Visibility.PRIMARY,
                    createdAt = datetime
                )
            }
            tags.forEach { repo.addNewTag(username, it) }

            val newBookmark = Bookmark(
                id = id,
                title = "title",
                url = url,
                tags = tags,
                createdAt = datetime
            )
            repo.addNewBookmark(username, newBookmark)
            val retrieved = repo.bookmark(username, id)
            assertEquals(newBookmark, retrieved)
        }

        @Test
        fun `add a bookmark with both primary and secondary tags`() {
            val id = "c05f3a01-347a-4441-99d9-87b00f6cfc4c"
            val url = "http://create.bookmark.with.primary.and.secondary.tags"

            val tags = listOf(
                "bdac1baa-fc1d-4365-a42e-e2671ce13fe0",
                "48287a9d-d59b-4587-a6de-7447d3971e0f",
                "69b314a4-3dca-413b-975b-220bbe149d1f",
                "8725eb3d-8865-4b33-bc78-aa249b121e88"
            ).mapIndexed {
                index, tagId ->
                Tag(
                    id = tagId,
                    name = tagId.split("-").first(),
                    visibility = if (index % 2 == 0) Visibility.PRIMARY else Visibility.SECONDARY,
                    createdAt = datetime
                )
            }
            tags.forEach { repo.addNewTag(username, it) }

            val newBookmark = Bookmark(
                id = id,
                title = "title",
                url = url,
                tags = tags,
                createdAt = datetime
            )
            repo.addNewBookmark(username, newBookmark)
            val expected = newBookmark.copy(
                tags = listOf(
                    tags.filter { it.visibility == Visibility.PRIMARY }.sortedBy { it.name },
                    tags.filter { it.visibility == Visibility.SECONDARY }.sortedBy { it.name }
                ).flatten()
            )
            val retrieved = repo.bookmark(username, id)
            assertEquals(expected, retrieved)
        }

        @Test
        fun `add two bookmarks with the same url without tags`() {
            val url = "7490c2f973cd"
            val first = Bookmark(
                id = "7a41be35-9896-4fe0-92ad-7490c2f973cd",
                title = "7a41be35",
                url = url,
                tags = emptyList(),
                createdAt = datetime
            )
            val second = Bookmark(
                id = "4caa2f08-7abc-4112-98a2-c39f65098319",
                title = "4caa2f08",
                url = url,
                tags = emptyList(),
                createdAt = datetime
            )

            repo.addNewBookmark(username, first)
            repo.addNewBookmark(username, second)

            assertEquals(repo.bookmark(username, first.id), first)
            assertEquals(repo.bookmark(username, second.id), null)
        }

        @Test
        fun `add two bookmarks with the same url with tags`() {
            val url = "7a713e38edf1"
            val first = Bookmark(
                id = "cbdf828b-774a-46d2-ac9d-7a713e38edf1",
                title = "cbdf828b",
                url = url,
                tags = emptyList(),
                createdAt = datetime
            )
            repo.addNewBookmark(username, first)

            val second = Bookmark(
                id = "4560bfee-ef10-4e66-9767-55fd6d2bdc28",
                title = "4560bfee",
                url = url,
                tags = listOf(
                    Tag(
                        id = "a5429671-9d68-41b1-b7c1-db5515ad923e",
                        name = "a5429671",
                        visibility = Visibility.PRIMARY,
                        createdAt = datetime
                    )

                ),
                createdAt = datetime
            )
            second.tags.forEach { repo.addNewTag(username, it) }
            repo.addNewBookmark(username, second)

            val retrieved = repo.bookmark(username, first.id)
            assertNotEquals(null, retrieved)
            assertEquals(
                Bookmark(
                    id = first.id,
                    title = first.title,
                    url = first.url,
                    tags = second.tags,
                    createdAt = first.createdAt
                ),
                retrieved
            )
        }

        @Test
        fun `select a bookmark by a tag`() {
            val tags = listOf(
                Tag(
                    id = "f1973f54-24d9-43a8-bd5e-09ddb18207f9",
                    name = "f1973f54-24d9-43a8-bd5e-09ddb18207f9",
                    visibility = Visibility.PRIMARY,
                    createdAt = datetime
                ),
                Tag(
                    id = "a458deaa-3048-4b7b-bdef-d8bd7de4d72c",
                    name = "a458deaa-3048-4b7b-bdef-d8bd7de4d72c",
                    visibility = Visibility.SECONDARY,
                    createdAt = datetime
                )
            )
            tags.forEach { repo.addNewTag(username, it) }

            val newBookmark = Bookmark(
                id = "c01a1361-0f80-406c-a358-063d3642731f",
                title = "title",
                url = "http://select.bookmark.by.a.tag",
                tags = tags,
                createdAt = datetime
            )
            repo.addNewBookmark(username, newBookmark)

            val tagIds = tags.map { it.id }
            val retrievedWithFirst = repo.bookmarks(username, listOf(tagIds.first()))
            assertTrue(retrievedWithFirst.contains(newBookmark))

            val retrievedWithSecond = repo.bookmarks(username, listOf(tagIds.drop(1).first()))
            assertTrue(retrievedWithSecond.contains(newBookmark))
        }

        @Test
        fun `select a bookmark by multiple tags`() {
            val tags = listOf(
                Tag(
                    id = "6ddd91d7-0b2a-4ae8-9e0e-5c92228a4929",
                    name = "6ddd91d7-0b2a-4ae8-9e0e-5c92228a4929",
                    visibility = Visibility.PRIMARY,
                    createdAt = datetime
                ),
                Tag(
                    id = "de2f6dc8-3d80-44d0-aa45-700a617726c2",
                    name = "de2f6dc8-3d80-44d0-aa45-700a617726c2",
                    visibility = Visibility.SECONDARY,
                    createdAt = datetime
                )
            )
            tags.forEach { repo.addNewTag(username, it) }

            val newBookmark = Bookmark(
                id = "6b190410-cbdf-4565-9d3f-fbd90db79da6",
                title = "title",
                url = "http://select.bookmark.by.multiple.tags",
                tags = tags,
                createdAt = datetime
            )
            repo.addNewBookmark(username, newBookmark)

            val anotherBookmark = Bookmark(
                id = "3b0a9212-38ce-42ba-a334-1d8067c7d182",
                title = "3b0a9212-38ce-42ba-a334-1d8067c7d182",
                url = "3b0a9212-38ce-42ba-a334-1d8067c7d182",
                tags = listOf(tags.first()),
                createdAt = datetime
            )
            repo.addNewBookmark(username, anotherBookmark)

            val retrieved = repo.bookmarks(username, tags.map { it.id })
            assertTrue(retrieved.contains(newBookmark))

            // Multiple tags should be interpreted as AND
            assertFalse(retrieved.contains(anotherBookmark))
        }
    }

    @Nested
    inner class Tags {
        private val username = "TestTags"

        @Test
        fun `create and delete tags`() {
            val tags = listOf(
                "68964f08-7b93-479c-aad0-1e6e89ed0367",
                "a4dcbfaf-bc95-4251-ad08-b454ed527b9a",
                "c39301f0-20f3-4247-be46-8f933b74b2ca"
            ).mapIndexed {
                index, tagId ->
                Tag(
                    id = tagId,
                    name = tagId.split("-").first(),
                    visibility = if (index % 2 == 0) Visibility.PRIMARY else Visibility.SECONDARY,
                    createdAt = datetime
                )
            }
            tags.forEach { repo.addNewTag(username, it) }
            val afterCreating = repo.tags(username)
            assertTrue(afterCreating.containsAll(tags))

            tags.forEach { repo.deleteTag(username, it.id) }
            val afterDeleting = repo.tags(username)
            assertTrue(afterDeleting.intersect(tags).isEmpty())
        }

        @Test
        fun `add and drop tags`() {
            val bookmark = Bookmark(
                id = "bf0e2635-1e78-4492-a1c6-196b8b46c10c",
                url = "http://add.and.drop.tags",
                tags = emptyList(),
                title = "title",
                createdAt = datetime
            )
            repo.addNewBookmark(username, bookmark)

            val tagsToAdd = listOf(
                "556dba7d-734a-4d2e-a3ee-dd1483dc80f5",
                "a76c6e52-d7a0-4b64-b8f8-c722e58537e2",
                "9fbf6ac9-65fe-4a48-9ddc-e7966fa9f94c"
            ).mapIndexed {
                index, tagId ->
                Tag(
                    id = tagId,
                    name = tagId.split("-").first(),
                    visibility = if (index % 2 == 0) Visibility.PRIMARY else Visibility.SECONDARY,
                    createdAt = datetime
                )
            }
            tagsToAdd.forEach { repo.addNewTag(username, it) }

            repo.addTagsToBookmark(username, bookmark.id, tagsToAdd.map { it.id }, datetime)
            val afterAdding = repo.bookmark(username, bookmark.id)
            assertEquals(tagsToAdd.toSet(), afterAdding!!.tags.toSet())

            val tagsToDrop = tagsToAdd.take(tagsToAdd.size - 1)
            repo.dropTagsFromBookmark(username, bookmark.id, tagsToDrop.map { it.id })
            val afterDropping = repo.bookmark(username, bookmark.id)
            assertEquals(tagsToAdd.toSet() - tagsToDrop.toSet(), afterDropping!!.tags.toSet())
        }
    }

    @Nested
    inner class Scores {
        private val username = "TestScores"

        @Test
        fun `add and update scores on bookmarks without tags`() {
            val bookmarks = listOf(
                "cca5485f-2ef2-4b60-bdbb-0e5136e67ca1",
                "4bcda54c-f832-4297-8741-30f35f51b537",
                "24f2c374-cad0-40c9-a4ee-5f5139753996",
                "83e0022b-1ef3-4e2f-ad64-104b5e24cca7"
            ).map {
                Bookmark(
                    id = it,
                    title = it.split("-").first(),
                    url = it.split("-").last(),
                    tags = emptyList(),
                    createdAt = datetime
                )
            }
            bookmarks.forEach { repo.addNewBookmark(username, it) }

            val oldScores = bookmarks.withIndex().map {
                BookmarkScore.make(bookmarkId = it.value.id, score = it.index / 10.0)
            }
            oldScores.forEach { repo.addOrUpdateScore(it) }

            val bookmarksWithOldScore = repo.bookmarks(username, tagIds = listOf())
            assertEquals(
                bookmarksWithOldScore.map { it.id },
                oldScores.sortedByDescending { it.score }.map { it.bookmarkId }
            )

            val newScores = oldScores.map { it.copy(score = 1.0 / it.score) }
            newScores.forEach { repo.addOrUpdateScore(it) }
            assertNotEquals(
                oldScores.sortedByDescending { it.score }.map { it.bookmarkId },
                newScores.sortedByDescending { it.score }.map { it.bookmarkId }
            )

            val bookmarksWithNewScore = repo.bookmarks(username, tagIds = listOf())
            assertEquals(
                bookmarksWithNewScore.map { it.id },
                newScores.sortedByDescending { it.score }.map { it.bookmarkId }
            )
        }

        @Test
        fun `add and update scores on bookmarks with a primary tag`() {
            val tag = Tag(
                id = "184017de-52b3-46ae-9f83-e22e7ce6297e",
                name = "184017de-52b3-46ae-9f83-e22e7ce6297e",
                visibility = Visibility.PRIMARY,
                createdAt = datetime
            )
            repo.addNewTag(username, tag)

            val bookmarks = listOf(
                "9911252e-a7a0-4098-9b96-fc635942c00d",
                "0dfaa544-d4d8-4a19-9997-de38996421eb",
                "653d212c-79cf-44b3-8826-b618c3455810",
                "a56e0ebf-4463-4192-beca-e07e6e4f1a30",
                "810a78f9-277d-4a26-a24a-f5d7b15370d6"
            ).map {
                Bookmark(
                    id = it,
                    title = it.split("-").first(),
                    url = it.split("-").last(),
                    tags = listOf(tag),
                    createdAt = datetime
                )
            }
            bookmarks.forEach { repo.addNewBookmark(username, it) }

            val oldScores = bookmarks.withIndex().map {
                BookmarkScore.make(bookmarkId = it.value.id, score = it.index / 10.0)
            }
            oldScores.forEach { repo.addOrUpdateScore(it) }

            val bookmarksWithOldScore = repo.bookmarks(username, tagIds = listOf(tag.id))
            assertEquals(
                bookmarksWithOldScore.map { it.id },
                oldScores.sortedByDescending { it.score }.map { it.bookmarkId }
            )

            val newScores = oldScores.map { it.copy(score = 1.0 / it.score) }
            newScores.forEach { repo.addOrUpdateScore(it) }
            assertNotEquals(
                oldScores.sortedByDescending { it.score }.map { it.bookmarkId },
                newScores.sortedByDescending { it.score }.map { it.bookmarkId }
            )

            val bookmarksWithNewScore = repo.bookmarks(username, tagIds = listOf(tag.id))
            assertEquals(
                bookmarksWithNewScore.map { it.id },
                newScores.sortedByDescending { it.score }.map { it.bookmarkId }
            )
        }
    }
}
