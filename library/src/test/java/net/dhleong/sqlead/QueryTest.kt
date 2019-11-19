package net.dhleong.sqlead

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

/**
 * @author dhleong
 */
class QueryTest : BaseDbTest() {

    @Test fun `Simple query`() {
        val result = db.query("""
            SELECT 42
            """
        ).use {
            it.getInt(0)
        }

        assertThat(result).isEqualTo(42)
    }

    @Test fun `Simple select`() {
        val (name, capacity) = db.query("""
            SELECT name, capacity
            FROM Ships
            """
        ).use {
            it.getString(0) to it.getInt(1)
        }

        assertThat(name).isEqualTo("Serenity")
        assertThat(capacity).isEqualTo(42)
    }

}