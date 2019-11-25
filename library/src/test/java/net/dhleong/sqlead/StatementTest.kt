package net.dhleong.sqlead

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNotNull
import assertk.assertions.isSuccess
import assertk.assertions.message
import org.junit.Test

/**
 * @author dhleong
 */
class StatementTest : BaseDbTest() {

    @Test fun `bindString with null should throw`() {
        db.compileStatement("""
            SELECT COUNT(*) FROM Pilots
            WHERE name = ?
        """.trimIndent()).use { stmt ->
            assertThat {
                stmt.bindString(1, null)
            }.isFailure().all {
                message().isNotNull().contains("null")
            }
        }
    }

    @Test fun `bindNull should work`() {
        db.compileStatement("""
            SELECT COUNT(*) FROM Pilots
            WHERE name = ?
        """.trimIndent()).use { stmt ->
            assertThat {
                stmt.bindNull(1)
                assertThat(stmt.simpleQueryForLong()).isEqualTo(0)
            }.isSuccess()
        }
    }

}
