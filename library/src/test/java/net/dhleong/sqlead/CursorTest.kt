package net.dhleong.sqlead

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import org.junit.Test

/**
 * @author dhleong
 */
class CursorTest : BaseDbTest() {

    @Test fun `Get various typed column values`() {
        db.query("""
            SELECT 42, 9001, 9002.3, 9004.5, "string", NULL
            """
        ).use {
            assert(it.getInt(0)).isEqualTo(42)
            assert(it.getLong(1)).isEqualTo(9001L)
            assert(it.getFloat(2)).isEqualTo(9002.3f)
            assert(it.getDouble(3)).isEqualTo(9004.5)
            assert(it.getString(4)).isEqualTo("string")
            assert(it.isNull(5)).isTrue()
        }
    }

    @Test fun `Test getCount`() {
        db.query("""
            SELECT * FROM Ships
            WHERE capacity = ?
            """,
            arrayOf(42)
        ).use { cursor ->
            assert(cursor.count).isEqualTo(1)
        }
    }

}