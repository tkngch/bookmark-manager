package tkngch.bookmarkManager.js.repository

import kotlinx.browser.window
import org.w3c.fetch.RequestInit
import tkngch.bookmarkManager.common.model.Username
import kotlin.js.Promise

interface UserRepository {
    fun getUsername(): Promise<Username>
}

class UserRepositoryImpl : UserRepository {
    private val endpoint = window.location.origin

    override fun getUsername() =
        window.fetch(
            "${this.endpoint}/api/user",
            init = RequestInit(method = "GET")
        ).then { it.text() }.then { it }
}
