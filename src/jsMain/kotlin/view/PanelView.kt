package tkngch.bookmarkManager.js.view

import kotlinx.browser.document
import kotlinx.dom.appendText
import kotlinx.dom.clear
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import tkngch.bookmarkManager.common.model.Username

interface PanelView {
    val containerDiv: HTMLElement

    fun render(username: Username)
    fun clear(): Unit = this.containerDiv.clear()
}

class PanelViewImpl : PanelView {
    override val containerDiv = document.getElementById("navbar-top-title") as HTMLAnchorElement
    private val textContainer = document.getElementById("navbar-top-text") as HTMLSpanElement

    override fun render(username: Username) {
        this.clear()
        this.containerDiv.appendText("Bookmark Manager")
        this.textContainer.appendText(username)
    }
}
