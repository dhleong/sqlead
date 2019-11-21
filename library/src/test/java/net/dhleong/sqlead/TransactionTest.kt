package net.dhleong.sqlead

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

/**
 * @author dhleong
 */
class TransactionTest : BaseDbTest() {
    @Test fun `inTransaction value for single transaction`() {
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(false)

        db.beginTransaction()
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(true)

        db.endTransaction()
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(false)
    }

    @Test fun `inTransaction value for nestedTransaction`() {
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(false)

        db.beginTransaction()
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(true)

        // still true
        db.beginTransaction()
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(true)

        // also still true
        db.setTransactionSuccessful()
        db.endTransaction()
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(true)

        // finally false
        db.endTransaction()
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(false)
    }

    @Test fun `Don't commit inner txn until outer txn finishes`() {
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(false)

        db.beginTransaction()
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(true)

        // still true
        db.beginTransaction()
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(true)

        db.insertPilots("Mal", "Inara")

        // also still true
        db.setTransactionSuccessful()
        db.endTransaction()
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(true)

        // query should be consistent within the transaction
        assertPilotsCount().isEqualTo(3)

        // finally false
        // NOTE: outer transaction fails
        db.endTransaction()
        assertThat(db.inTransaction(), "inTransaction").isEqualTo(false)

        assertPilotsCount().isEqualTo(1)
    }

    @Test fun `Rollback outer transaction from inner`() {
        db.beginTransaction()

        // inner transaction fails:
        db.beginTransaction()
        db.endTransaction()

        // outer transaction is successful
        db.insertPilots("Mal", "Inara")
        db.setTransactionSuccessful()
        db.endTransaction()

        // because the inner transaction fails, the whole transaction should rollback
        assertPilotsCount().isEqualTo(1)
    }

    private fun assertPilotsCount() = assertThat(pilotsCount(), "pilots count")

    private fun pilotsCount() = db.query(
            """
            SELECT COUNT(*) FROM Pilots
        """.trimIndent()
        ).use {
            it.getInt(0)
        }
}