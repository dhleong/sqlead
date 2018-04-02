package net.dhleong.sqlead

import assertk.assert
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
            it.getInt(1)
        }

        assert(result).isEqualTo(42)
    }

    @Test fun `Simple select`() {
        val (name, capacity) = db.query("""
            SELECT name, capacity
            FROM Ships
            """
        ).use {
            it.getString(1) to it.getInt(2)
        }

        assert(name).isEqualTo("Serenity")
        assert(capacity).isEqualTo(42)
    }

}