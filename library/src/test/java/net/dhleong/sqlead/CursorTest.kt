package net.dhleong.sqlead

import android.database.Cursor
import assertk.assert
import assertk.assertions.containsExactly
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

    @Test fun `Test getColumnCount`() {
        db.query("""
            SELECT "a", 2, 3.0
            """
        ).use { cursor ->
            assert(cursor.columnCount).isEqualTo(3)
        }
    }

    @Test fun `Test getType`() {
        db.query("""
            SELECT "a", 2, 3.0, NULL
            """
        ).use { cursor ->
            assert(cursor.getType(0)).isEqualTo(Cursor.FIELD_TYPE_STRING)
            assert(cursor.getType(1)).isEqualTo(Cursor.FIELD_TYPE_INTEGER)
            assert(cursor.getType(2)).isEqualTo(Cursor.FIELD_TYPE_FLOAT)
            assert(cursor.getType(3)).isEqualTo(Cursor.FIELD_TYPE_NULL)
        }
    }

    @Test fun `Test getColumnNames`() {
        db.query("""
            SELECT "a" AS string, 2 AS int, 3.0 AS float
            """
        ).use { cursor ->
            assert(cursor.columnNames).containsExactly(
                "string",
                "int",
                "float"
            )
        }
    }

    @Test fun `Test getColumnName`() {
        db.query("""
            SELECT "a" AS string, 2 AS int, 3.0 AS float
            """
        ).use { cursor ->
            assert(cursor.getColumnName(0)).isEqualTo("string")
            assert(cursor.getColumnName(1)).isEqualTo("int")
            assert(cursor.getColumnName(2)).isEqualTo("float")
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