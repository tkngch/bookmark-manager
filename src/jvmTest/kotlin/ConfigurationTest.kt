package tkngch.bookmarkManager.jvm.configuration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConfigurationTest {

    @Test
    fun `get the development configuration`() {
        val config = Configuration.getInstance(AppEnv.DEVELOPMENT)
        assertEquals(2, config.userTable.table.entries.size)
        assertTrue(config.jdbcSqliteURL.contains(":memory:"))
    }
}
