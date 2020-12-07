package tkngch.bookmarkManager.js.view

import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.input
import kotlinx.html.js.div
import kotlinx.html.js.h5
import kotlinx.html.js.li
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.ul
import kotlinx.html.label
import kotlinx.html.p
import kotlinx.html.span
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.js.binder.TagBinder

interface TagsView {
    val containerDiv: HTMLDivElement
    val binder: TagBinder
    val selectedTags: MutableSet<Tag>

    fun render(tags: List<Tag>, showHidden: Boolean = false)
    fun clear(): Unit = this.containerDiv.clear()
}

class TagsViewImpl(private val title: String) : TagsView {

    override val containerDiv = document.getElementById("side-panel") as HTMLDivElement
    override val binder = TagBinder()
    override val selectedTags = mutableSetOf<Tag>()
    init {
        this.containerDiv.appendChild(document.create.p { +"Loading" })
    }

    override fun render(tags: List<Tag>, showHidden: Boolean) {
        this.clear()
        this.containerDiv.appendChild(this.makeTitleView())
        TagsEditModal.render(this.binder, tags)

        val visibleTagContainer = document.create.ul { classes = setOf("nav", "flex-column") }
        tags.filter { it.visibility == Visibility.PRIMARY }.forEach {
            visibleTagContainer.appendChild(this.makeTagView(it))
        }
        this.containerDiv.appendChild(visibleTagContainer)

        if (showHidden) {
            this.containerDiv.appendChild(this.makeShowLessView())
            val hiddenTagContainer = document.create.ul { classes = setOf("nav", "flex-column") }
            tags.filter { it.visibility == Visibility.SECONDARY }.forEach {
                hiddenTagContainer.appendChild(this.makeTagView(it))
            }
            this.containerDiv.appendChild(hiddenTagContainer)
        } else {
            this.containerDiv.appendChild(this.makeShowMoreView())
        }
    }

    private fun makeTitleView() = document.create.h5 {
        +title

        button {
            type = ButtonType.button
            classes = setOf("btn", "btn-outline-dark", "btn-sm", "ml-2")
            +"Edit"

            onClickFunction = { _ -> TagsEditModal.show() }
        }
    }

    private fun makeTagView(tag: Tag) = document.create.li {
        div {
            val inputDivId = tag.id
            classes = setOf("custom-control", "custom-switch")
            input {
                type = InputType.checkBox
                classes = setOf("custom-control-input")
                attributes["id"] = inputDivId
                checked = this@TagsViewImpl.selectedTags.contains(tag)
            }
            label {
                classes = setOf("custom-control-label")
                attributes["for"] = inputDivId
                +tag.name
            }

            onChangeFunction = { _: Event ->
                val targetDiv = document.getElementById(inputDivId) as HTMLInputElement
                if (targetDiv.checked) {
                    this@TagsViewImpl.selectedTags.add(tag)
                } else {
                    this@TagsViewImpl.selectedTags.remove(tag)
                }
                this@TagsViewImpl.binder.onClick.trigger(tag)
            }
        }
    }

    private fun makeShowMoreView() = document.create.div {
        classes = setOf("font-weight-light")
        attributes["id"] = "TagsView.makeShowMoreView"
        span {
            +"Show More"
            onClickFunction = { _: Event -> this@TagsViewImpl.binder.onClickShowMore.trigger() }
        }
    }

    private fun makeShowLessView() = document.create.div {
        classes = setOf("font-weight-light")
        attributes["id"] = "TagsView.makeShowLessView"
        span {
            +"Show Less"
            onClickFunction = { _: Event -> this@TagsViewImpl.binder.onClickShowLess.trigger() }
        }
    }
}
