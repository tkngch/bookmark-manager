package tkngch.bookmarkManager.common.config

data class Configuration(val appEnv: AppEnv, val port: Int)

private const val isDevelopment = true

enum class AppEnv { PRODUCTION, DEVELOPMENT; }

val config = Configuration(
    appEnv = if (isDevelopment) AppEnv.DEVELOPMENT else AppEnv.PRODUCTION,
    port = if (isDevelopment) 8080 else 33874
)
