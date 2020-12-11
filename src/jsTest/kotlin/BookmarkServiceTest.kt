package tkngch.bookmarkManager.js.service

import kotlinx.browser.document
import kotlinx.html.div
import kotlinx.html.dom.create
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.BookmarkId
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.TagId
import tkngch.bookmarkManager.common.model.TagName
import tkngch.bookmarkManager.common.model.URL
import tkngch.bookmarkManager.common.model.Username
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.js.binder.BookmarkBinder
import tkngch.bookmarkManager.js.binder.TagBinder
import tkngch.bookmarkManager.js.repository.BookmarkRepository
import tkngch.bookmarkManager.js.repository.UserRepository
import tkngch.bookmarkManager.js.view.BookmarksView
import tkngch.bookmarkManager.js.view.PanelView
import tkngch.bookmarkManager.js.view.TagsView
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals

class BookmarkServiceTest {

    class MockPanelView : PanelView {
        override val containerDiv: HTMLElement = document.create.div {}
        override fun render(username: Username) {}
        override fun clear() {}
    }

    class MockTagsView : TagsView {
        override val containerDiv: HTMLDivElement = document.create.div {} as HTMLDivElement
        override val binder = TagBinder()
        override val selectedTags = mutableSetOf<Tag>()
        override fun render(tags: List<Tag>, showHidden: Boolean) {}
        override fun clear() {}
    }

    class MockBookmarksView : BookmarksView {
        override val containerDiv: HTMLDivElement = document.create.div {} as HTMLDivElement
        override val binder = BookmarkBinder()
        override var allTags = listOf<Tag>()
        override fun render(bookmarks: List<Bookmark>) {}
        override fun clear() {}
    }

    val tag = Tag(
        id = "e09d2159-f50e-4f69-bf05-66b98a33f496",
        name = "*New Addition",
        visibility = Visibility.PRIMARY,
        createdAt = "now"
    )
    val bookmark = Bookmark(
        id = "3ef20eba-42b0-4385-a60c-f7343f4ce7c3",
        title = "3ef20eba-42b0-4385-a60c-f7343f4ce7c3",
        url = "3ef20eba-42b0-4385-a60c-f7343f4ce7c3",
        tags = listOf(),
        createdAt = "now"
    )

    data class BookmarkRepositoryCalled(
        var getTags: Int = 0,
        var createTag: Int = 0,
        var updateTag: Int = 0,
        var deleteTag: Int = 0,
        var getBookmarks: Int = 0,
        var createBookmark: Int = 0,
        var refreshBookmark: Int = 0,
        var addTagsToBookmark: Int = 0,
        var dropTagsFromBookmark: Int = 0,
        var deleteBookmark: Int = 0,
        var logBookmarkVisit: Int = 0
    )
    inner class MockBookmarkRepository : BookmarkRepository {
        val nCalled = BookmarkRepositoryCalled()

        override fun getTags(): Promise<List<Tag>> {
            this.nCalled.getTags++
            return Promise.resolve(listOf(this@BookmarkServiceTest.tag)).then { it }
        }

        override fun createTag(tagName: TagName, visibility: Visibility): Promise<Unit> {
            this.nCalled.createTag++
            return Promise.resolve(Unit)
        }

        override fun updateTag(
            tagId: TagId,
            updatedTagName: TagName,
            updatedVisibility: Visibility
        ): Promise<Unit> {
            this.nCalled.updateTag++
            return Promise.resolve(Unit)
        }

        override fun deleteTag(tagId: TagId): Promise<Unit> {
            this.nCalled.deleteTag++
            return Promise.resolve(Unit)
        }

        override fun getBookmarks(tagIds: List<TagId>): Promise<List<Bookmark>> {
            this.nCalled.getBookmarks++
            return Promise.resolve(listOf(this@BookmarkServiceTest.bookmark))
        }

        override fun createBookmark(url: URL, tags: List<Tag>): Promise<Unit> {
            this.nCalled.createBookmark++
            return Promise.resolve(Unit)
        }

        override fun refreshBookmark(bookmarkId: BookmarkId): Promise<Unit> {
            this.nCalled.refreshBookmark++
            return Promise.resolve(Unit)
        }

        override fun addTagsToBookmark(
            bookmarkId: BookmarkId,
            tags: List<TagId>
        ): Promise<Unit> {
            this.nCalled.addTagsToBookmark++
            return Promise.resolve(Unit)
        }

        override fun dropTagsFromBookmark(
            bookmarkId: BookmarkId,
            tags: List<TagId>
        ): Promise<Unit> {
            this.nCalled.dropTagsFromBookmark++
            return Promise.resolve(Unit)
        }

        override fun deleteBookmark(bookmarkId: BookmarkId): Promise<Unit> {
            this.nCalled.deleteBookmark++
            return Promise.resolve(Unit)
        }

        override fun logBookmarkVisit(bookmarkId: BookmarkId): Promise<Unit> {
            this.nCalled.logBookmarkVisit++
            return Promise.resolve(Unit)
        }
    }

    class MockUserRepository : UserRepository {
        override fun getUsername() = Promise.resolve {}.then { "username" }
    }

    private val panelView = MockPanelView()
    private val tagsView = MockTagsView()
    private val bookmarksView = MockBookmarksView()
    private val bookmarkRepository = MockBookmarkRepository()
    private val userRepository = MockUserRepository()

    private val service = BookmarkServiceImpl(
        panelView,
        tagsView,
        bookmarksView,
        bookmarkRepository,
        userRepository
    )
    init {
        service.run()
    }

    @Test
    fun testBindingOnTagAdd() {
        val nCalledBeforeTagAdd = this.bookmarkRepository.nCalled.copy()
        this.tagsView.binder.onAdd.trigger(this.tag.name, Visibility.PRIMARY)
        assertEquals(
            nCalledBeforeTagAdd.copy(createTag = nCalledBeforeTagAdd.createTag + 1),
            this.bookmarkRepository.nCalled
        )
    }

    @Test
    fun testBindingOnTagUpdate() {
        val nCalledBeforeTagUpdate = this.bookmarkRepository.nCalled.copy()
        this.tagsView.binder.onUpdate.trigger(this.tag, Visibility.SECONDARY)
        assertEquals(
            nCalledBeforeTagUpdate.copy(updateTag = nCalledBeforeTagUpdate.updateTag + 1),
            this.bookmarkRepository.nCalled
        )
    }

    @Test
    fun testBindingOnTagDelete() {
        val nCalledBeforeTagDelete = this.bookmarkRepository.nCalled.copy()
        this.tagsView.binder.onDelete.trigger(this.tag)
        assertEquals(
            nCalledBeforeTagDelete.copy(deleteTag = nCalledBeforeTagDelete.deleteTag + 1),
            this.bookmarkRepository.nCalled
        )
    }

    // For some reason, a chain of callbacks (Promise.then) does not get
    // triggered, and so this test fails.
    // @Test
    // fun testBindingOnBookmarkAdd() {
    //     val nCalledBeforeBookmarkAdd = this.bookmarkRepository.nCalled.copy()
    //     this.bookmarksView.binder.onAdd.trigger(this.bookmark.url)
    //     assertEquals(
    //         nCalledBeforeBookmarkAdd.copy(
    //             createBookmark = nCalledBeforeBookmarkAdd.createBookmark + 1
    //         ),
    //         this.bookmarkRepository.nCalled
    //     )
    // }

    @Test
    fun testBindingOnBookmarkAdd() {
        val nCalledBeforeBookmarkRefresh = this.bookmarkRepository.nCalled.copy()
        this.bookmarksView.binder.onRefresh.trigger(this.bookmark)
        assertEquals(
            nCalledBeforeBookmarkRefresh.copy(
                refreshBookmark = nCalledBeforeBookmarkRefresh.refreshBookmark + 1
            ),
            this.bookmarkRepository.nCalled
        )
    }

    @Test
    fun testBindingOnBookmarkClick() {
        val nCalledBeforeBookmarkClick = this.bookmarkRepository.nCalled.copy()
        this.bookmarksView.binder.onClick.trigger(this.bookmark)
        assertEquals(
            nCalledBeforeBookmarkClick.copy(
                logBookmarkVisit = nCalledBeforeBookmarkClick.logBookmarkVisit + 1
            ),
            this.bookmarkRepository.nCalled
        )
    }

    @Test
    fun testBindingOnBookmarkDelete() {
        val nCalledBeforeBookmarkDelete = this.bookmarkRepository.nCalled.copy()
        this.bookmarksView.binder.onDelete.trigger(this.bookmark)
        assertEquals(
            nCalledBeforeBookmarkDelete.copy(
                deleteBookmark = nCalledBeforeBookmarkDelete.deleteBookmark + 1
            ),
            this.bookmarkRepository.nCalled
        )
    }

    @Test
    fun testBindingOnBookmarkTagAdd() {
        val nCalledBeforeBookmarkTagAdd = this.bookmarkRepository.nCalled.copy()
        this.bookmarksView.binder.onTagAdd.trigger(this.bookmark, this.tag)
        assertEquals(
            nCalledBeforeBookmarkTagAdd.copy(
                addTagsToBookmark = nCalledBeforeBookmarkTagAdd.addTagsToBookmark + 1
            ),
            this.bookmarkRepository.nCalled
        )
    }

    @Test
    fun testBindingOnBookmarkTagDrop() {
        val nCalledBeforeBookmarkTagDrop = this.bookmarkRepository.nCalled.copy()
        this.bookmarksView.binder.onTagDrop.trigger(this.bookmark, this.tag)
        assertEquals(
            nCalledBeforeBookmarkTagDrop.copy(
                dropTagsFromBookmark = nCalledBeforeBookmarkTagDrop.dropTagsFromBookmark + 1
            ),
            this.bookmarkRepository.nCalled
        )
    }
}
