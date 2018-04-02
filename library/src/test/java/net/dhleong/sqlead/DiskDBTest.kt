package net.dhleong.sqlead

import assertk.assert
import assertk.assertions.isEqualTo
import org.junit.Test

/**
 * @author dhleong
 */
class DiskDBTest : BaseDbTest(inMemory = false) {
    @Test fun `Initialize DB on disk successfully`() {
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