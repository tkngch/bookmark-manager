package tkngch.bookmarkManager.js.binder

import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.URL

data class BookmarkBinder(
    val onAdd: Binder1<URL> = Binder1(),
    val onClick: Binder1<Bookmark> = Binder1(),
    val onRefresh: Binder1<Bookmark> = Binder1(),
    val onDelete: Binder1<Bookmark> = Binder1(),
    val onTagAdd: Binder2<Bookmark, Tag> = Binder2(),
    val onTagDrop: Binder2<Bookmark, Tag> = Binder2()
)
