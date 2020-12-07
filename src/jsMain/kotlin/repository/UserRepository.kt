package tkngch.bookmarkManager.js.repository

import kotlinx.browser.window
import org.w3c.fetch.RequestInit
import tkngch.bookmarkManager.common.config.config
import tkngch.bookmarkManager.common.model.URL
import tkngch.bookmarkManager.common.model.Username
import kotlin.js.Promise

interface UserRepository {
    fun getUsername(): Promise<Username>
}

class UserRepositoryImpl(
    private val apiBaseURL: URL = "http://localhost:${config.port}"
) : UserRepository {

    override fun getUsername() =
        window.fetch(
            "${this.apiBaseURL}/api/user",
            init = RequestInit(method = "GET")
        ).then { it.text() }.then { it }
}
