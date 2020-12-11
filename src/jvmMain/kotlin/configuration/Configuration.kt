package tkngch.bookmarkManager.jvm.configuration

import io.ktor.auth.UserHashedTableAuth
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tkngch.bookmarkManager.common.model.Username
import java.io.File
import java.security.MessageDigest
import java.util.Base64

enum class AppEnv { PRODUCTION, DEVELOPMENT; }

data class Configuration(val jdbcSqliteURL: String, val userTable: UserHashedTableAuth) {

    companion object Factory {
        fun getInstance(appEnv: AppEnv) = when (appEnv) {
            AppEnv.DEVELOPMENT -> Configuration(
                "jdbc:sqlite::memory:",
                getUserTable("dev_users.json")
            )
            AppEnv.PRODUCTION -> Configuration(
                "jdbc:sqlite:${getDatabase()}",
                getUserTable("users.json")
            )
        }

        @Serializable
        private data class User(val username: Username, val password: String)

        private val base64Decoder = Base64.getDecoder()
        private val digest = MessageDigest.getInstance("SHA-256")

        private fun getUserTable(filename: String) = UserHashedTableAuth(
            digester = { password -> digest.digest(password.toByteArray()) },
            table = getUsers(filename).fold(mapOf()) { acc, curr ->
                acc + mapOf(curr.username to base64Decoder.decode(curr.password))
            }
        )

        private fun getUsers(filename: String) = Json.decodeFromString<List<User>>(
            {}.javaClass.getResource("/$filename").readText()
        )

        private fun getDatabase(): String {
            val parentDirectory = System.getenv("XDG_DATA_HOME")?.let { File(it) }
                ?: File(System.getenv("HOME")!!).resolve(".local/share")

            val dataDirectory = parentDirectory.resolve("bookmark-manager")
            if (!dataDirectory.exists()) dataDirectory.mkdir()

            return dataDirectory.resolve("data.sqlite3").toString()
        }
    }
}
