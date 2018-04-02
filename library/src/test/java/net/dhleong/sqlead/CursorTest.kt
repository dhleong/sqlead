package net.dhleong.sqlead

import assertk.assert
import assertk.assertions.isEqualTo
import org.junit.Test

/**
 * @author dhleong
 */
class CursorTest : BaseDbTest() {

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