package tkngch.bookmarkManager.js.view

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement

interface Modal {
    val backdropId: String
    val modalId: String

    fun show() {
        val backdrop = document.getElementById(this.backdropId) as HTMLDivElement
        val modal = document.getElementById(this.modalId) as HTMLDivElement
        backdrop.style.display = "block"
        modal.style.display = "block"
        modal.className += "show"
        window.onclick = { event ->
            if (event.target == modal) {
                console.log("Closing the modal...")
                backdrop.style.display = "none"
                modal.style.display = "none"
                modal.className = modal.className.replace("show", "")
            }
        }
    }
}
