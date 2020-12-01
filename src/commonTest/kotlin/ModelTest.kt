package tkngch.bookmarkManager.common.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelTest {
    @Test
    fun testEncodingAndDecodingBookmark() {
        val datetime = "2020-11-29T12:37:59.871738Z"
        val bookmark = Bookmark(
            id = "f3e25846-09ac-4f83-aa06-cce96b6d39c4",
            title = "title",
            url = "f3e25846-09ac-4f83-aa06-cce96b6d39c4",
            tags = listOf(
                Tag(
                    id = "6e8ccf94-54e0-46a2-af3e-8b48c57f6f7a",
                    name = "6e8ccf94-54e0-46a2-af3e-8b48c57f6f7a",
                    visibility = Visibility.PRIMARY,
                    createdAt = datetime
                ),
                Tag(
                    id = "f4aaa75d-3c2c-42e8-9279-2045f690294c",
                    name = "f4aaa75d-3c2c-42e8-9279-2045f690294c",
                    visibility = Visibility.PRIMARY,
                    createdAt = datetime
                ),
            ),
            createdAt = "2020-11-29T12:37:59.871738Z"
        )
        val encoded = Json.encodeToString(bookmark)
        val decoded = Json.decodeFromString<Bookmark>(encoded)
        assertEquals(bookmark, decoded)
    }
}
