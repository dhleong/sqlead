package net.dhleong.sqlead

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import assertk.assert
import assertk.assertions.isEqualTo
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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
            it.getString(0) to it.getInt(1)
        }

        assert(name).isEqualTo("Serenity")
        assert(capacity).isEqualTo(42)
    }

    @Test fun `Trigger onUpgrade`() {
        db.close()

        val calledCreate = AtomicBoolean(false)
        val calledUpgradeTo = AtomicInteger(-1)
        db = createDbUsingCallback(
            object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    calledCreate.set(true)
                }

                override fun onUpgrade(db: SupportSQLiteDatabase?, oldVersion: Int, newVersion: Int) {
                    calledUpgradeTo.set(newVersion)
                }
            }
        )

        assert(calledCreate.get()).isEqualTo(false)
        assert(calledUpgradeTo.get()).isEqualTo(2)
    }

    @Test fun `Trigger onDowngrade`() {
        db.close()

        val calledCreate = AtomicBoolean(false)
        val calledDowngradeTo = AtomicInteger(-1)
        db = createDbUsingCallback(
            object : SupportSQLiteOpenHelper.Callback(0) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    calledCreate.set(true)
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase?,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    throw UnsupportedOperationException()
                }

                override fun onDowngrade(
                    db: SupportSQLiteDatabase?,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    calledDowngradeTo.set(newVersion)
                }
            }
        )

        assert(calledCreate.get()).isEqualTo(false)
        assert(calledDowngradeTo.get()).isEqualTo(0)
    }
}