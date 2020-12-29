package tkngch.bookmarkManager.jvm.domain

import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.Visibility
import java.time.Instant
import java.util.UUID

fun Tag.Companion.make(name: String, visibility: Visibility) =
    Tag(
        id = UUID.randomUUID().toString(),
        name = name,
        visibility = visibility,
        createdAt = Instant.now().toString()
    )
