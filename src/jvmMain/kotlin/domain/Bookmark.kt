package tkngch.bookmarkManager.jvm.domain

import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.URL
import java.time.Instant
import java.util.UUID

fun Bookmark.Companion.make(title: String, url: URL, tags: List<Tag>) =
    Bookmark(
        id = UUID.randomUUID().toString(),
        title = title,
        url = url,
        tags = tags,
        createdAt = Instant.now().toString()
    )
