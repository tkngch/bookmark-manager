package tkngch.bookmarkManager.jvm.configuration

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigurationTest {

    @Test
    fun `get the development configuration`() {
        val config = Configuration.getInstance(AppEnv.DEVELOPMENT)
        assertEquals(2, config.userTable.table.entries.size)
        assertTrue(config.jdbcSqliteURL.contains(":memory:"))
    }
}
