package tkngch.bookmarkManager.js

import kotlinx.browser.window
import tkngch.bookmarkManager.js.repository.BookmarkRepository
import tkngch.bookmarkManager.js.repository.BookmarkRepositoryImpl
import tkngch.bookmarkManager.js.repository.UserRepository
import tkngch.bookmarkManager.js.repository.UserRepositoryImpl
import tkngch.bookmarkManager.js.service.BookmarkService
import tkngch.bookmarkManager.js.service.BookmarkServiceImpl
import tkngch.bookmarkManager.js.view.BookmarksView
import tkngch.bookmarkManager.js.view.BookmarksViewImpl
import tkngch.bookmarkManager.js.view.PanelView
import tkngch.bookmarkManager.js.view.PanelViewImpl
import tkngch.bookmarkManager.js.view.TagsView
import tkngch.bookmarkManager.js.view.TagsViewImpl

fun main() {
    window.onload = { App().start() }
}

class App {
    private val panelView: PanelView
    private val tagsView: TagsView
    private val bookmarksView: BookmarksView
    private val bookmarkRepository: BookmarkRepository
    private val userRepository: UserRepository
    private val service: BookmarkService

    init {
        panelView = PanelViewImpl()
        tagsView = TagsViewImpl("Tags")
        bookmarksView = BookmarksViewImpl("Bookmarks")
        bookmarkRepository = BookmarkRepositoryImpl()
        userRepository = UserRepositoryImpl()
        service = BookmarkServiceImpl(
            panelView,
            tagsView,
            bookmarksView,
            bookmarkRepository,
            userRepository
        )
    }

    fun start() = this.service.run()
}
