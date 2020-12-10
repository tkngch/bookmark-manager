package tkngch.bookmarkManager.js.view

import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.form
import kotlinx.html.h5
import kotlinx.html.h6
import kotlinx.html.input
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseOutFunction
import kotlinx.html.js.onMouseOverFunction
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.js.binder.BookmarkBinder

interface BookmarksView {
    val containerDiv: HTMLDivElement
    val binder: BookmarkBinder
    var allTags: List<Tag>

    fun render(bookmarks: List<Bookmark>)
    fun clear(): Unit = this.containerDiv.clear()
}

class BookmarksViewImpl(private val title: String) : BookmarksView {

    override val containerDiv = document.getElementById("main-panel") as HTMLDivElement
    override val binder = BookmarkBinder()
    override var allTags: List<Tag> = listOf()
    init {
        this.containerDiv.appendChild(document.create.p { +"Loading" })
    }

    override fun render(bookmarks: List<Bookmark>) {
        this.clear()
        this.containerDiv.appendChild(this.makeTitleView())
        this.containerDiv.appendChild(this.makeBookmarkAddInput())
        bookmarks.forEach { this.containerDiv.appendChild(this.makeBookmarkView(it)) }
    }

    private fun makeTitleView() = document.create.h5 {
        +title
    }

    private fun makeBookmarkAddInput() = document.create.div {
        form {
            val textInputId = "newBookmarkInput"
            div {
                classes = setOf("form-row")
                div {
                    classes = setOf("col-md-6")

                    input {
                        type = InputType.text
                        classes = setOf("form-control")
                        attributes["id"] = textInputId
                        placeholder = "URL"
                    }
                }
                div {
                    classes = setOf("col-auto")
                    button {
                        type = ButtonType.button
                        classes = setOf("btn", "btn-outline-dark")
                        +"Add a new bookmark"
                    }
                    onClickFunction = { _: Event ->
                        val textInput = document.getElementById(textInputId) as HTMLInputElement
                        if (textInput.value.isNotEmpty()) binder.onAdd.trigger(textInput.value)
                        textInput.value = ""
                    }
                }
            }
        }
    }

    private fun makeBookmarkView(bookmark: Bookmark): HTMLDivElement = document.create.div {
        val divId = bookmark.id

        div {
            classes = setOf("border", "p-2", "my-1")
            attributes["id"] = divId

            button {
                type = ButtonType.button
                classes = setOf("btn", "btn-outline-dark", "btn-sm", "float-right")
                +"Delete"
                onClickFunction = { _ -> this@BookmarksViewImpl.binder.onDelete.trigger(bookmark) }
            }

            div {
                style = "cursor: pointer"
                h6 { +bookmark.title }
                span { +bookmark.url }

                onClickFunction = { _ -> this@BookmarksViewImpl.binder.onClick.trigger(bookmark) }
            }

            div {
                bookmark.tags.map { tag ->
                    span {
                        classes = setOf("badge", "badge-pill", "badge-secondary", "m-1")
                        +tag.name
                    }
                }
                button {
                    type = ButtonType.button
                    classes = setOf("btn", "btn-outline-dark", "btn-sm", "ml-2")
                    +"Edit"
                    onClickFunction = { _ ->
                        BookmarkEditModal.render(
                            this@BookmarksViewImpl.binder,
                            bookmark,
                            this@BookmarksViewImpl.allTags
                        )
                        BookmarkEditModal.show()
                    }
                }
            }
        }

        val focusCssClass = "border-dark"
        onMouseOverFunction = { _: Event ->
            val targetDiv = document.getElementById(divId) as HTMLDivElement
            targetDiv.className += " $focusCssClass"
        }
        onMouseOutFunction = { _: Event ->
            val targetDiv = document.getElementById(divId) as HTMLDivElement
            targetDiv.className = targetDiv.className.replace(focusCssClass, "")
        }
    }
}
