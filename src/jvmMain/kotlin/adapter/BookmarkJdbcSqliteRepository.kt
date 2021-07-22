package tkngch.bookmarkManager.jvm.adapter

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.sqlite.SQLiteErrorCode
import org.sqlite.SQLiteException
import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.BookmarkId
import tkngch.bookmarkManager.common.model.Datetime
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.TagId
import tkngch.bookmarkManager.common.model.TagName
import tkngch.bookmarkManager.common.model.URL
import tkngch.bookmarkManager.common.model.Username
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.common.model.VisitLog
import tkngch.bookmarkManager.jvm.domain.BookmarkRepository
import tkngch.bookmarkManager.jvm.domain.BookmarkScore
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import javax.sql.PooledConnection

class BookmarkJdbcSqliteRepository(
    private val connectionPool: PooledConnection
) : BookmarkRepository {

    init {
        Migration.migrateV1(connectionPool.connection)
    }

    private val separator = ";"

    override fun addNewBookmark(user: Username, bookmark: Bookmark) =
        this.connectionPool.connection.use { connection ->
            doAddNewBookmark(user, bookmark, connection)
        }

    private fun doAddNewBookmark(user: Username, bookmark: Bookmark, connection: Connection) {
        try {
            Mutation.insertNewBookmark(user, bookmark, connection)
        } catch (err: SQLiteException) {
            if (err.resultCode == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                // the URL is already in the database for this user.
            } else {
                throw err
            }
        } finally {
            // When the URL is already in the database, `bookmark.id` does not match the corresponding ID in the database.
            bookmark.tags.forEach { tag ->
                Mutation.addTagToBookmarkByURL(
                    user,
                    bookmark.url,
                    tag.id,
                    bookmark.createdAt,
                    connection
                )
            }
        }
    }

    override fun addNewTag(user: Username, tag: Tag) {
        this.connectionPool.connection.use { connection ->
            Mutation.insertNewTag(user, tag, connection)
        }
    }

    override fun addBookmarkVisitLog(user: Username, log: VisitLog) {
        this.connectionPool.connection.use { connection ->
            Mutation.insertNewLog(user, log, connection)
        }
    }

    override fun bookmark(user: Username, id: BookmarkId): Bookmark? =
        this.connectionPool.connection.use { connection ->
            Query.selectBookmarkById(user, id, this.separator, connection).use { resultSet ->
                this.decodeBookmarks(resultSet).firstOrNull()
            }
        }

    override fun bookmarks(user: Username, tagIds: List<TagId>): List<Bookmark> =
        if (tagIds.isEmpty()) {
            this.bookmarksWithoutTags(user)
        } else {
            this.bookmarksWithTags(user, tagIds)
        }

    private fun bookmarksWithoutTags(user: Username): List<Bookmark> =
        this.connectionPool.connection.use { connection ->
            Query.selectBookmarksWithoutTags(user, connection).use { resultSet ->
                this.decodeBookmarks(resultSet)
            }
        }

    private fun bookmarksWithTags(user: Username, tagIds: List<TagId>): List<Bookmark> =
        this.connectionPool.connection.use { connection ->
            Query.selectBookmarksByTags(user, tagIds, this.separator, connection).use { resultSet ->
                this.decodeBookmarks(resultSet)
            }
        }

    override fun tag(user: Username, id: TagId): Tag =
        this.connectionPool.connection.use { connection ->
            Query.selectTagById(user, id, connection).use { resultSet ->
                this.decodeTags(resultSet).first()
            }
        }

    override fun tags(user: Username): List<Tag> =
        this.connectionPool.connection.use { connection ->
            Query.selectTags(user, connection).use { resultSet ->
                this.decodeTags(resultSet)
            }
        }

    override fun addTagsToBookmark(
        user: Username,
        bookmarkId: BookmarkId,
        tagIds: List<TagId>,
        createdAt: Datetime
    ) = this.connectionPool.connection.use { connection ->
        tagIds.forEach { tagId ->
            Mutation.addTagToBookmark(user, bookmarkId, tagId, createdAt, connection)
        }
    }

    override fun dropTagsFromBookmark(user: Username, bookmarkId: BookmarkId, tagIds: List<TagId>) =
        this.connectionPool.connection.use { connection ->
            tagIds.forEach { tagId ->
                Mutation.dropTagFromBookmark(user, bookmarkId, tagId, connection)
            }
        }

    override fun updateTag(
        user: Username,
        tagId: TagId,
        updatedTagName: TagName,
        updatedVisibility: Visibility
    ) {
        this.connectionPool.connection.use { connection ->
            Mutation.updateTag(user, tagId, updatedTagName, updatedVisibility, connection)
        }
    }

    override fun deleteBookmark(user: Username, id: BookmarkId) {
        this.connectionPool.connection.use { connection ->
            Mutation.deleteBookmark(user, id, connection)
        }
    }

    override fun deleteTag(user: Username, id: TagId) {
        this.connectionPool.connection.use { connection ->
            Mutation.deleteTag(user, id, connection)
        }
    }

    override fun visitLogs(user: Username): List<VisitLog> =
        this.connectionPool.connection.use { connection ->
            Query.selectLogs(user, connection).use { resultSet ->
                val logs: MutableList<VisitLog> = mutableListOf()

                while (resultSet.next()) {
                    logs.add(
                        VisitLog(
                            bookmarkId = resultSet.getString("bookmarkId"),
                            visitedAt = resultSet.getString("visitedAt")
                        )
                    )
                }
                logs.toList()
            }
        }

    override fun addOrUpdateScores(scores: List<BookmarkScore>) {
        this.connectionPool.connection.use { connection ->
            scores.forEach { score -> Mutation.upsertScore(score, connection) }
        }
    }

    private fun decodeBookmarks(resultSet: ResultSet): List<Bookmark> {
        val bookmarks: MutableList<Bookmark> = mutableListOf()

        while (resultSet.next()) {
            bookmarks.add(
                Bookmark(
                    id = resultSet.getString("bookmarkId"),
                    title = resultSet.getString("title"),
                    url = resultSet.getString("url"),
                    tags = this.decodeTagsFromJsonString(resultSet.getString("tags")),
                    createdAt = resultSet.getString("createdAt")
                )
            )
        }
        return bookmarks.toList()
    }

    private fun decodeTags(resultSet: ResultSet): List<Tag> {
        val tags: MutableList<Tag> = mutableListOf()

        while (resultSet.next()) {
            tags.add(
                Tag(
                    id = resultSet.getString("id"),
                    name = resultSet.getString("name"),
                    visibility = Visibility.valueOf(resultSet.getString("visibility")),
                    createdAt = resultSet.getString("createdAt")
                )
            )
        }
        return tags.toList()
    }

    private fun decodeTagsFromJsonString(jsonString: String): List<Tag> {
        val unsorted: List<Tag> = if (jsonString.isEmpty()) {
            emptyList()
        } else {
            jsonString.split(this.separator).map { Json.decodeFromString(it) }
        }

        return listOf(
            unsorted.filter { it.visibility == Visibility.PRIMARY }.sortedBy { it.name },
            unsorted.filter { it.visibility == Visibility.SECONDARY }.sortedBy { it.name }
        ).flatten()
    }

    private object Query {
        fun selectBookmarkById(
            user: Username,
            bookmarkId: BookmarkId,
            separator: String,
            dbConnection: Connection
        ): ResultSet {
            val statement = dbConnection.prepareStatement(
                """
                WITH tagless AS (
                    SELECT id, url, title, createdAt
                    FROM bookmark
                    WHERE username = ?  -- 1
                        AND id NOT IN (SELECT bookmarkId FROM bookmarkTag WHERE tagId IN (SELECT id FROM tag WHERE username = ?))  -- 2
                        AND id = ?  -- 3
                ),
                bookmarkIDsWithTags AS (
                    SELECT
                        bookmarkTag.bookmarkId AS bookmarkId,
                        tag.id AS tagId,
                        tag.name AS name,
                        tag.visibility AS visibility,
                        printf('{"id": "%s", "name": "%s", "visibility": "%s", "createdAt": "%s"}', tag.id, tag.name, tag.visibility, tag.createdAt) AS tagAsJson
                    FROM bookmarkTag
                    LEFT JOIN tag ON bookmarkTag.tagId = tag.id
                    WHERE tag.username = ? AND bookmarkTag.bookmarkId = ?  -- 4, 5
                )
                SELECT
                    selected.bookmarkId AS bookmarkId,
                    selected.url AS url,
                    selected.title AS title,
                    selected.tags AS tags,
                    selected.createdAt AS createdAt
                FROM (
                    SELECT
                        tagged.id AS bookmarkId,
                        tagged.url AS url,
                        tagged.title AS title,
                        tagged.createdAt AS createdAt,
                        GROUP_CONCAT(bookmarkIDsWithTags.tagAsJson, ?) AS tags  -- 6
                    FROM bookmarkIDsWithTags
                    INNER JOIN bookmark AS tagged ON bookmarkIDsWithTags.bookmarkId = tagged.id
                    GROUP BY tagged.id
                    UNION
                    SELECT
                        tagless.id AS bookmarkId,
                        tagless.url AS url,
                        tagless.title AS title,
                        tagless.createdAt AS createdAt,
                        "" AS tags
                    FROM tagless
                ) AS selected
                """.trimIndent()
            )
            statement.setString(1, user)
            statement.setString(2, user)
            statement.setString(3, bookmarkId)
            statement.setString(4, user)
            statement.setString(5, bookmarkId)
            statement.setString(6, separator)
            statement.closeOnCompletion()
            return statement.executeQuery()
        }

        fun selectBookmarksWithoutTags(username: Username, dbConnection: Connection): ResultSet {
            val statement = dbConnection.prepareStatement(
                """
                SELECT
                    bookmark.id AS bookmarkId,
                    bookmark.url AS url,
                    bookmark.title AS title,
                    "" AS tags,
                    bookmark.createdAt AS createdAt
                FROM bookmark
                LEFT JOIN score ON bookmark.id = score.bookmarkId
                WHERE bookmark.username = ?  -- 1
                    AND bookmark.id NOT IN (
                        SELECT bookmarkId FROM bookmarkTag
                        WHERE tagId IN (SELECT id FROM tag WHERE username = ?)  -- 2
                    )
                -- Place bookmarks with score = NULL last (IS NULL returns 0 for non-NULL entries). Then order by scores and dates.
                ORDER BY score.score IS NULL, score.score DESC, bookmark.createdAt DESC
                """.trimIndent()
            )
            statement.setString(1, username)
            statement.setString(2, username)
            statement.closeOnCompletion()
            return statement.executeQuery()
        }

        fun selectBookmarksByTags(
            username: Username,
            tagIds: List<TagId>,
            separator: String,
            dbConnection: Connection
        ): ResultSet {
            val tagIdsPlaceholder = tagIds.map { "?" }.reduce { acc, str -> "$acc, $str" }
            val statement = dbConnection.prepareStatement(
                """
                WITH bookmarkIDsWithTags AS (
                    SELECT
                        bookmarkTag.bookmarkId AS bookmarkId,
                        tag.id AS tagId,
                        tag.name AS name,
                        tag.visibility AS visibility,
                        printf('{"id": "%s", "name": "%s", "visibility": "%s", "createdAt": "%s"}', tag.id, tag.name, tag.visibility, tag.createdAt) AS tagAsJson
                    FROM bookmarkTag
                    LEFT JOIN tag ON bookmarkTag.tagId = tag.id
                    WHERE tag.username = ?  -- 1
                ),
                relevantBookmarkIDs AS (
                    SELECT bookmarkId, COUNT(tagId) AS nMatchingTags
                    FROM bookmarkTag
                    WHERE tagId IN ($tagIdsPlaceholder)  -- 2, 3, ..., n + 1
                    GROUP BY bookmarkId
                )
                SELECT
                    tagged.id AS bookmarkId,
                    tagged.url AS url,
                    tagged.title AS title,
                    tagged.createdAt AS createdAt,
                    GROUP_CONCAT(bookmarkIDsWithTags.tagAsJson, ?) AS tags  -- n + 2
                FROM bookmarkIDsWithTags
                INNER JOIN bookmark AS tagged ON bookmarkIDsWithTags.bookmarkId = tagged.id
                LEFT JOIN score ON bookmarkIDsWithTags.bookmarkId = score.bookmarkId
                WHERE
                    tagged.id IN (SELECT bookmarkId FROM relevantBookmarkIDs WHERE nMatchingTags >= ?)  -- n + 3
                GROUP BY tagged.id
                ORDER BY score.score IS NULL, score.score DESC, tagged.createdAt DESC
                """.trimIndent()
            )
            statement.setString(1, username)
            // Note `index` below starts with zero.
            tagIds.withIndex().forEach { statement.setString(it.index + 2, it.value) }
            statement.setString(tagIds.size + 2, separator)
            statement.setInt(tagIds.size + 3, tagIds.size)
            statement.closeOnCompletion()
            return statement.executeQuery()
        }

        fun selectTags(username: Username, dbConnection: Connection): ResultSet {
            val statement = dbConnection.prepareStatement(
                """
                SELECT id, name, visibility, createdAt
                FROM tag
                WHERE username = ?
                """.trimIndent()
            )
            statement.setString(1, username)
            statement.closeOnCompletion()
            return statement.executeQuery()
        }

        fun selectTagById(username: Username, tagId: TagId, dbConnection: Connection): ResultSet {
            val statement = dbConnection.prepareStatement(
                """
                SELECT id, name, visibility, createdAt
                FROM tag
                WHERE username = ? AND id = ?
                """.trimIndent()
            )
            statement.setString(1, username)
            statement.setString(2, tagId)
            statement.closeOnCompletion()
            return statement.executeQuery()
        }

        fun selectLogs(username: Username, dbConnection: Connection): ResultSet {
            val statement = dbConnection.prepareStatement(
                """
                SELECT bookmarkId, visitedAt
                FROM bookmarkVisitLog
                WHERE username = ?
                """.trimIndent()
            )
            statement.setString(1, username)
            return statement.executeQuery()
        }
    }

    private object Mutation {
        fun insertNewBookmark(username: Username, bookmark: Bookmark, dbConnection: Connection) =
            dbConnection.prepareStatement(
                """
                INSERT INTO bookmark (id, username, url, title, createdAt)
                SELECT ?, ?, ?, ?, ?  -- 1, 2, 3, 4, 5
                WHERE NOT EXISTS (SELECT 1 FROM bookmark WHERE username = ? AND url = ?)  -- 6, 7
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, bookmark.id)
                statement.setString(2, username)
                statement.setString(3, bookmark.url)
                statement.setString(4, bookmark.title)
                statement.setString(5, bookmark.createdAt)
                statement.setString(6, username)
                statement.setString(7, bookmark.url)
                statement.executeUpdate()
            }

        fun deleteBookmark(username: Username, bookmarkId: BookmarkId, dbConnection: Connection) =
            dbConnection.prepareStatement(
                """
                DELETE FROM bookmark WHERE username = ? AND id = ?
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, username)
                statement.setString(2, bookmarkId)
                statement.executeUpdate()
            }

        fun insertNewTag(username: Username, tag: Tag, dbConnection: Connection) =
            dbConnection.prepareStatement(
                """
                INSERT INTO tag (id, username, name, visibility, createdAt)
                SELECT ?, ?, ?, ?, ?  -- 1, 2, 3, 4, 5
                WHERE NOT EXISTS (SELECT 1 FROM tag WHERE username = ? AND name = ?)  -- 6, 7
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, tag.id)
                statement.setString(2, username)
                statement.setString(3, tag.name)
                statement.setString(4, tag.visibility.toString())
                statement.setString(5, tag.createdAt)
                statement.setString(6, username)
                statement.setString(7, tag.name)
                statement.executeUpdate()
            }

        fun deleteTag(username: Username, tagId: TagId, dbConnection: Connection) =
            dbConnection.prepareStatement(
                """
                DELETE FROM tag
                WHERE username = ? AND id = ?
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, username)
                statement.setString(2, tagId)
                statement.executeUpdate()
            }

        fun insertNewLog(username: Username, log: VisitLog, dbConnection: Connection) =
            dbConnection.prepareStatement(
                """
                INSERT INTO bookmarkVisitLog (username, bookmarkId, visitedAt)
                VALUES (?, ?, ?)
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, username)
                statement.setString(2, log.bookmarkId)
                statement.setString(3, log.visitedAt)
                statement.executeUpdate()
            }

        fun addTagToBookmark(
            username: Username,
            bookmarkId: BookmarkId,
            tagId: TagId,
            createdAt: Datetime,
            dbConnection: Connection
        ) = dbConnection.prepareStatement(
            """
            INSERT INTO bookmarkTag (bookmarkId, tagId, createdAt)
            SELECT ?, ?, ?  -- 1, 2, 3
            WHERE NOT EXISTS (SELECT 1 FROM bookmarkTag WHERE bookmarkId = ? AND tagId = ?)  -- 4, 5
                AND EXISTS (SELECT 1 FROM bookmark WHERE username = ? AND id = ?)  -- 6, 7
                AND EXISTS (SELECT 1 FROM tag WHERE username = ? AND id = ?)  -- 8, 9
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, bookmarkId)
            statement.setString(2, tagId)
            statement.setString(3, createdAt)
            statement.setString(4, bookmarkId)
            statement.setString(5, tagId)
            statement.setString(6, username)
            statement.setString(7, bookmarkId)
            statement.setString(8, username)
            statement.setString(9, tagId)
            statement.executeUpdate()
        }

        fun addTagToBookmarkByURL(
            username: Username,
            url: URL,
            tagId: TagId,
            createdAt: Datetime,
            dbConnection: Connection
        ) = dbConnection.prepareStatement(
            """
            INSERT INTO bookmarkTag (bookmarkId, tagId, createdAt)
            SELECT id, ?, ?  -- 1, 2
            FROM bookmark
            WHERE
                username = ?  -- 3
                AND url = ?  -- 4
                AND NOT EXISTS (SELECT 1 FROM bookmarkTag WHERE bookmarkId IN (SELECT id FROM bookmark WHERE username = ? AND url = ?) AND tagId = ?)  -- 5, 6, 7
                AND EXISTS (SELECT 1 FROM bookmark WHERE username = ? AND url = ?)  -- 8, 9
                AND EXISTS (SELECT 1 FROM tag WHERE username = ? AND id = ?)  -- 10, 11
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, tagId)
            statement.setString(2, createdAt)
            statement.setString(3, username)
            statement.setString(4, url)
            statement.setString(5, username)
            statement.setString(6, url)
            statement.setString(7, tagId)
            statement.setString(8, username)
            statement.setString(9, url)
            statement.setString(10, username)
            statement.setString(11, tagId)
            statement.executeUpdate()
        }

        fun dropTagFromBookmark(
            username: Username,
            bookmarkId: BookmarkId,
            tagId: TagId,
            dbConnection: Connection
        ) = dbConnection.prepareStatement(
            """
             DELETE FROM bookmarkTag
             WHERE bookmarkId = ?  -- 1
                 AND tagId = ?  -- 2
                 AND EXISTS (SELECT 1 FROM bookmark WHERE username = ? AND id = ?)  -- 3, 4
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, bookmarkId)
            statement.setString(2, tagId)
            statement.setString(3, username)
            statement.setString(4, bookmarkId)
            statement.executeUpdate()
            statement.close()
        }

        fun updateTag(
            username: Username,
            tagId: TagId,
            tagName: TagName,
            visibility: Visibility,
            dbConnection: Connection
        ) = dbConnection.prepareStatement(
            """
            UPDATE OR IGNORE tag
            SET name = ?, visibility = ?  -- 1, 2
            WHERE username = ? AND id = ?  -- 3, 4
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, tagName)
            statement.setString(2, visibility.toString())
            statement.setString(3, username)
            statement.setString(4, tagId)
            statement.executeUpdate()
        }

        fun upsertScore(score: BookmarkScore, dbConnection: Connection) =
            dbConnection.prepareStatement(
                """
                INSERT OR REPLACE INTO score (bookmarkId, score, updatedAt) VALUES (?, ?, ?)
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, score.bookmarkId)
                statement.setDouble(2, score.score)
                statement.setString(3, score.updatedAt)
                statement.executeUpdate()
            }
    }

    private object Migration {
        fun migrateV1(dbConnection: Connection) = dbConnection.createStatement().use { statement ->
            val schemaVersion = try {
                statement.executeQuery("PRAGMA user_version").getLong(1)
            } catch (err: SQLException) {
                0
            }

            if (schemaVersion < 1) {
                this.createTableBookmark(statement)
                this.createTableTag(statement)
                this.createTableBookmarkTag(statement)
                this.createTableBookmarkVisitLog(statement)
                this.createTableScore(statement)
                statement.executeUpdate("PRAGMA user_version = 1")
            }
        }

        private fun createTableBookmark(statement: Statement) =
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS bookmark (
                    id TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    url TEXT NOT NULL,
                    title TEXT NOT NULL,
                    createdAt TEXT NOT NULL,
                    UNIQUE(username, url)
                )
                """.trimIndent()
            )

        private fun createTableTag(statement: Statement) =
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS tag (
                    id TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    name TEXT NOT NULL,
                    visibility TEXT NOT NULL,
                    createdAt TEXT NOT NULL,
                    UNIQUE(username, name)
                )
                """.trimIndent()
            )

        private fun createTableBookmarkTag(statement: Statement) =
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS bookmarkTag (
                    bookmarkId TEXT NOT NULL,
                    tagId TEXT NOT NULL,
                    createdAt TEXT NOT NULL,
                    PRIMARY KEY(bookmarkId, tagId),
                    FOREIGN KEY(bookmarkId) REFERENCES bookmark(id) ON DELETE CASCADE,
                    FOREIGN KEY(tagId) REFERENCES tag(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )

        private fun createTableBookmarkVisitLog(statement: Statement) =
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS bookmarkVisitLog(
                    username TEXT NOT NULL,
                    bookmarkId TEXT NOT NULL,
                    visitedAt TEXT NOT NULL,
                    PRIMARY KEY (username, visitedAt)
                )
                """.trimIndent()
            )

        private fun createTableScore(statement: Statement) =
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS score(
                    bookmarkId TEXT PRIMARY KEY,
                    score REAL NOT NULL,
                    updatedAt TEXT NOT NULL,
                    FOREIGN KEY (bookmarkId) REFERENCES bookmark(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
    }
}
