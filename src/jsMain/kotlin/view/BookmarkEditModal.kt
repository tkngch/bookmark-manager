package tkngch.bookmarkManager.js.view

import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.DIV
import kotlinx.html.InputType
import kotlinx.html.br
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.h5
import kotlinx.html.input
import kotlinx.html.js.onChangeFunction
import kotlinx.html.label
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import tkngch.bookmarkManager.common.model.Bookmark
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.js.binder.Binder2
import tkngch.bookmarkManager.js.binder.BookmarkBinder

object BookmarkEditModal : Modal {
    override val backdropId = "backdrop"
    override val modalId = "bookmarkTagsEditModal"
    private val headerDiv = document.getElementById("bookmarkTagsEditModalHeader") as HTMLDivElement
    private val bodyDiv = document.getElementById("bookmarkTagsEditModalBody") as HTMLDivElement

    fun render(binder: BookmarkBinder, bookmark: Bookmark, allTags: List<Tag>) {
        this.headerDiv.clear()
        this.headerDiv.appendChild(
            document.create.h5 {
                classes = setOf("modal-title")
                +bookmark.title
            }
        )
        this.bodyDiv.clear()
        this.bodyDiv.appendChild(this.makeFormToEditTags(binder, bookmark, allTags))
    }

    private fun makeFormToEditTags(
        binder: BookmarkBinder,
        bookmark: Bookmark,
        allTags: List<Tag>
    ) = document.create.div {
        +"Add or drop tags"

        div {
            classes = setOf("px-5")

            allTags.filter { it.visibility == Visibility.PRIMARY }.map { tag ->
                tagCheckBox(
                    bookmark,
                    tag,
                    binder.onTagAdd,
                    binder.onTagDrop
                )()
            }

            br {}

            allTags.filter { it.visibility == Visibility.SECONDARY }.map { tag ->
                tagCheckBox(
                    bookmark,
                    tag,
                    binder.onTagAdd,
                    binder.onTagDrop
                )()
            }
        }
    }

    private fun tagCheckBox(
        bookmark: Bookmark,
        tag: Tag,
        onTagAdd: Binder2<Bookmark, Tag>,
        onTagDrop: Binder2<Bookmark, Tag>
    ): DIV.() -> Unit = {
        val checkboxInputId = "checkboxFor${tag.id}"
        div {
            classes = setOf("m-2", "form-check", "form-check-inline")
            input {
                classes = setOf("form-check-input")
                type = InputType.checkBox
                attributes["id"] = checkboxInputId
                checked = bookmark.tags.contains(tag)
            }
            label {
                classes = setOf("form-check-label")
                attributes["for"] = checkboxInputId
                +tag.name
            }

            onChangeFunction = { _: Event ->
                val targetDiv = document.getElementById(checkboxInputId) as HTMLInputElement
                if (targetDiv.checked) {
                    onTagAdd.trigger(bookmark, tag)
                } else {
                    onTagDrop.trigger(bookmark, tag)
                }
            }
        }
    }
}
