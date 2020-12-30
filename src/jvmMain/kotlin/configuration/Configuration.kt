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
                "jdbc:sqlite:$developmentDatabase",
                developmentUserTable
            )
            AppEnv.PRODUCTION -> Configuration(
                "jdbc:sqlite:$productionDatabase",
                productionUserTable
            )
        }

        private val developmentUserTable: UserHashedTableAuth by lazy {
            getUserTable({}.javaClass.getResource("/dev_users.json").readText())
        }

        private val productionUserTable: UserHashedTableAuth by lazy {
            getUserTable(productionDataDirectory.resolve("users.json").readText())
        }

        private fun getUserTable(usersJson: String): UserHashedTableAuth {
            @Serializable
            data class User(val username: Username, val password: String)
            val users = Json.decodeFromString<List<User>>(usersJson)

            val base64Decoder = Base64.getDecoder()
            val digest = MessageDigest.getInstance("SHA-256")

            return UserHashedTableAuth(
                digester = { password -> digest.digest(password.toByteArray()) },
                table = users.fold(mapOf()) { acc, curr ->
                    acc + mapOf(curr.username to base64Decoder.decode(curr.password))
                }
            )
        }

        private val developmentDatabase: String = ":memory:"

        private val productionDatabase: String by lazy {
            productionDataDirectory.resolve("data.sqlite3").toString()
        }

        private val productionDataDirectory: File by lazy {
            val parentDirectory = System.getenv("XDG_DATA_HOME")?.let { File(it) }
                ?: File(System.getenv("HOME")!!).resolve(".local/share")

            val dataDirectory = parentDirectory.resolve("bookmark-manager")
            if (!dataDirectory.exists()) dataDirectory.mkdir()

            dataDirectory
        }
    }
}
