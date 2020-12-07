package tkngch.bookmarkManager.js.service

import kotlinx.browser.window
import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.TagName
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.js.repository.BookmarkRepository
import tkngch.bookmarkManager.js.repository.UserRepository
import tkngch.bookmarkManager.js.view.BookmarksView
import tkngch.bookmarkManager.js.view.PanelView
import tkngch.bookmarkManager.js.view.TagsView
import kotlin.js.Promise

interface BookmarkService {
    fun run()
}

class BookmarkServiceImpl(
    private val panelView: PanelView,
    private val tagsView: TagsView,
    private val bookmarksView: BookmarksView,
    private val bookmarkRepository: BookmarkRepository,
    private val userRepository: UserRepository
) : BookmarkService {

    private var tags: List<Tag> = listOf()
    private var isHiddenTagsVisible: Boolean = false

    init {
        // Tags
        this.tagsView.binder.onAdd.subscribe {
            tagName: TagName, visibility: Visibility ->
            this.bookmarkRepository.createTag(tagName, visibility).then {
                this.fetchAndShowTags()
            }
        }
        this.tagsView.binder.onUpdate.subscribe {
            tag: Tag, newVisibility: Visibility ->
            this.bookmarkRepository.updateTag(tag.id, tag.name, newVisibility).then {
                this.fetchAndShowTags()
            }
        }

        this.tagsView.binder.onClick.subscribe { this.fetchAndShowBookmarks() }

        this.tagsView.binder.onDelete.subscribe { tagToDelete ->
            this.bookmarkRepository.deleteTag(tagToDelete.id).then {
                this.tagsView.selectedTags.removeAll { tag -> tag.id == tagToDelete.id }
                this.fetchAndShowTags()
                this.fetchAndShowBookmarks()
            }
        }

        this.tagsView.binder.onClickShowMore.subscribe {
            this.isHiddenTagsVisible = true
            this.tagsView.render(this.tags, this.isHiddenTagsVisible)
        }

        this.tagsView.binder.onClickShowLess.subscribe {
            this.isHiddenTagsVisible = false
            this.tagsView.render(this.tags, this.isHiddenTagsVisible)
        }

        // bookmarks
        this.bookmarksView.binder.onAdd.subscribe { url ->
            this.getTagForNewBookmark().then { tag ->
                this.bookmarkRepository.createBookmark(
                    url,
                    listOf(tag) + this.tagsView.selectedTags.toList()
                )
            }.then { this.fetchAndShowBookmarks() }
        }

        this.bookmarksView.binder.onClick.subscribe { bookmark ->
            this.bookmarkRepository.logBookmarkVisit(bookmark.id)
        }
        this.bookmarksView.binder.onClick.subscribe { bookmark ->
            window.open(bookmark.url, "_blank")
        }

        this.bookmarksView.binder.onDelete.subscribe { bookmark ->
            this.bookmarkRepository.deleteBookmark(bookmark.id).then {
                this.fetchAndShowBookmarks()
            }
        }
        this.bookmarksView.binder.onTagAdd.subscribe { bookmark, tag ->
            this.bookmarkRepository.addTagsToBookmark(bookmark.id, listOf(tag.id)).then {
                this.fetchAndShowBookmarks()
            }
        }
        this.bookmarksView.binder.onTagDrop.subscribe { bookmark, tag ->
            this.bookmarkRepository.dropTagsFromBookmark(bookmark.id, listOf(tag.id)).then {
                this.fetchAndShowBookmarks()
            }
        }
    }

    private fun getTagForNewBookmark(): Promise<Tag> {
        val tagName: TagName = "*New Addition"
        val found = this.tags.filter { it.name == tagName }
        return if (found.isEmpty()) {
            this.bookmarkRepository.createTag(tagName, Visibility.PRIMARY).then {
                this.fetchAndShowTags()
            }.then { this.getTagForNewBookmark() }.then { it }
        } else {
            Promise.resolve(found.first())
        }
    }

    private fun fetchAndShowTags() = this.bookmarkRepository.getTags().then { tags ->
        this.tags = tags
        this.bookmarksView.allTags = tags
        this.tagsView.render(this.tags, this.isHiddenTagsVisible)
    }

    private fun fetchAndShowBookmarks() =
        this.bookmarkRepository.getBookmarks(
            this.tagsView.selectedTags.map { it.id }
        ).then { bookmarks ->
            val bookmarksToShow: List<Bookmark> = if (this.tagsView.selectedTags.isNotEmpty()) {
                bookmarks
            } else {
                bookmarks.filter { bookmark ->
                    bookmark.tags.all { it.visibility == Visibility.PRIMARY }
                }
            }
            this.bookmarksView.render(bookmarksToShow)
        }

    override fun run() {
        this.fetchAndShowTags().then { this.getTagForNewBookmark() }.then { tag ->
            this.tagsView.selectedTags.add(tag)
        }.then {
            this.fetchAndShowBookmarks()
        }
        this.userRepository.getUsername().then { this.panelView.render(it) }
    }

    private fun seed() {
        this.tagsView.binder.onAdd.trigger("News", Visibility.PRIMARY)
        this.tagsView.binder.onAdd.trigger("Shopping", Visibility.PRIMARY)
        this.tagsView.binder.onAdd.trigger("Tech", Visibility.SECONDARY)
        this.bookmarksView.binder.onAdd.trigger("https://kotlinlang.org/")
    }
}
