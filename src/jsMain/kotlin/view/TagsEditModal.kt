package tkngch.bookmarkManager.js.view

import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.ButtonType
import kotlinx.html.DIV
import kotlinx.html.FORM
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.form
import kotlinx.html.h5
import kotlinx.html.hr
import kotlinx.html.input
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseOutFunction
import kotlinx.html.js.onMouseOverFunction
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.style
import kotlinx.html.ul
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import tkngch.bookmarkManager.common.model.Tag
import tkngch.bookmarkManager.common.model.TagName
import tkngch.bookmarkManager.common.model.Visibility
import tkngch.bookmarkManager.js.binder.Binder1
import tkngch.bookmarkManager.js.binder.Binder2
import tkngch.bookmarkManager.js.binder.TagBinder

object TagsEditModal : Modal {
    override val backdropId = "backdrop"
    override val modalId = "tagEditModal"
    private val headerDiv = document.getElementById("tagEditModalHeader") as HTMLDivElement
    private val bodyDiv = document.getElementById("tagEditModalBody") as HTMLDivElement

    init {
        this.headerDiv.clear()
        this.headerDiv.appendChild(
            document.create.h5 {
                classes = setOf("modal-title")
                +"Edit Tags"
            }
        )
    }

    fun render(binder: TagBinder, tags: List<Tag>) {
        this.bodyDiv.clear()

        this.bodyDiv.appendChild(FormToAddTags.render(binder.onAdd))
        this.bodyDiv.appendChild(document.create.hr {})
        this.bodyDiv.appendChild(FormToEditTags.render(tags, binder.onUpdate, binder.onDelete))
    }
}

private object FormToAddTags {
    private const val textInputId = "newTagInput"
    private const val checkboxInputId = "newTagVisibilityInput"

    fun render(onAdd: Binder2<TagName, Visibility>) = document.create.div {
        +"Create a new tag"

        ul {
            style = "list-style-type:none;"
            classes = setOf("mt-1")

            li {
                form {
                    this@FormToAddTags.textInputDiv()()
                    this@FormToAddTags.checkboxInputDiv()()
                    this@FormToAddTags.submitButton(onAdd)()
                }
            }
        }
    }

    private fun textInputDiv(): FORM.() -> Unit = {
        div {
            classes = setOf("form-group")
            input {
                type = InputType.text
                classes = setOf("form-control")
                attributes["id"] = textInputId
                placeholder = "new tag"
            }
        }
    }

    private fun checkboxInputDiv(): FORM.() -> Unit = {
        div {
            classes = setOf("form-group")
            div {
                classes = setOf("form-check")

                input {
                    type = InputType.checkBox
                    classes = setOf("form-check-input")
                    attributes["id"] = checkboxInputId
                }
                label {
                    classes = setOf("form-check-label")
                    attributes["for"] = checkboxInputId
                    +"This is a secondary tag."
                }
            }
        }
    }

    private fun submitButton(onAdd: Binder2<TagName, Visibility>): FORM.() -> Unit = {
        button {
            classes = setOf("btn", "btn-outline-dark")
            +"Submit"

            onClickFunction = { event ->
                event.preventDefault()

                val tagInput = document.getElementById(textInputId) as HTMLInputElement
                val tagVisibility = document.getElementById(checkboxInputId) as HTMLInputElement

                if (tagInput.value.isNotEmpty()) {
                    onAdd.trigger(
                        tagInput.value,
                        if (tagVisibility.checked) Visibility.SECONDARY else Visibility.PRIMARY
                    )
                }

                tagInput.value = ""
            }
        }
    }
}

private object FormToEditTags {

    fun render(tags: List<Tag>, onUpdate: Binder2<Tag, Visibility>, onDelete: Binder1<Tag>) =
        document.create.div {
            +"Edit tags"
            div {
                classes = setOf("container")

                listOf(
                    tags.filter { it.visibility == Visibility.PRIMARY },
                    tags.filter { it.visibility == Visibility.SECONDARY }
                ).flatten().map { tag -> editButtons(tag, onUpdate, onDelete)() }
            }
        }

    private fun editButtons(
        tag: Tag,
        onUpdate: Binder2<Tag, Visibility>,
        onDelete: Binder1<Tag>
    ): DIV.() -> Unit = {
        val divId = "editTagRow${tag.id}"
        div {
            classes = setOf("row", "mt-1")
            div {
                classes = setOf("col")
                attributes["id"] = divId
                +tag.name
            }

            this@FormToEditTags.editVisibilityButton(tag, onUpdate)()
            this@FormToEditTags.deleteTagButton(tag, onDelete)()

            val focusClass = "font-weight-bold bg-light"
            onMouseOverFunction = { _: Event ->
                val targetDiv = document.getElementById(divId) as HTMLDivElement
                targetDiv.className += " $focusClass"
            }
            onMouseOutFunction = { _: Event ->
                val targetDiv = document.getElementById(divId) as HTMLDivElement
                targetDiv.className = targetDiv.className.replace(focusClass, "")
            }
        }
    }

    private fun editVisibilityButton(
        tag: Tag,
        onUpdate: Binder2<Tag, Visibility>
    ): DIV.() -> Unit = {
        val isPrimary = tag.visibility == Visibility.PRIMARY
        div {
            classes = setOf("col")
            button {
                type = ButtonType.button
                classes = setOf("btn", "btn-outline-dark", "btn-sm")
                if (isPrimary) +"Mark secondary" else +"Mark primary"
            }
            onClickFunction = { _: Event ->
                val newVisibility = if (isPrimary) Visibility.SECONDARY else Visibility.PRIMARY
                onUpdate.trigger(tag, newVisibility)
            }
        }
    }

    private fun deleteTagButton(tag: Tag, onDelete: Binder1<Tag>): DIV.() -> Unit = {
        div {
            classes = setOf("col")
            button {
                type = ButtonType.button
                classes = setOf("btn", "btn-outline-dark", "btn-sm")
                +"Delete"
            }

            onClickFunction = { _: Event -> onDelete.trigger(tag) }
        }
    }
}
