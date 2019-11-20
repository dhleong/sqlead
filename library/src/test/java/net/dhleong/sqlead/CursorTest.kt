package net.dhleong.sqlead

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
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
            assertThat(it.getInt(0)).isEqualTo(42)
            assertThat(it.getLong(1)).isEqualTo(9001L)
            assertThat(it.getFloat(2)).isEqualTo(9002.3f)
            assertThat(it.getDouble(3)).isEqualTo(9004.5)
            assertThat(it.getString(4)).isEqualTo("string")
            assertThat(it.isNull(5)).isTrue()
        }
    }

    @Test fun `Test getColumnCount`() {
        db.query("""
            SELECT "a", 2, 3.0
            """
        ).use { cursor ->
            assertThat(cursor.columnCount).isEqualTo(3)
        }
    }

    @Test fun `Test getType`() {
        db.query("""
            SELECT "a", 2, 3.0, NULL
            """
        ).use { cursor ->
            assertThat(cursor.getType(0)).isEqualTo(Cursor.FIELD_TYPE_STRING)
            assertThat(cursor.getType(1)).isEqualTo(Cursor.FIELD_TYPE_INTEGER)
            assertThat(cursor.getType(2)).isEqualTo(Cursor.FIELD_TYPE_FLOAT)
            assertThat(cursor.getType(3)).isEqualTo(Cursor.FIELD_TYPE_NULL)
        }
    }

    @Test fun `Test getColumnIndex`() {
        db.query("""
            SELECT "a" AS string, 2 AS int, 3.0 AS float
            """
        ).use { cursor ->
            assertThat(cursor.getColumnIndex("string")).isEqualTo(0)
            assertThat(cursor.getColumnIndex("int")).isEqualTo(1)
            assertThat(cursor.getColumnIndex("float")).isEqualTo(2)
            assertThat(cursor.getColumnIndex("notThere")).isEqualTo(-1)

            assertThat {
                cursor.getColumnIndexOrThrow("notThere")
            }.isFailure().all {
                // the API interface specifies that an IllegalArgumentException be thrown
                isInstanceOf(IllegalArgumentException::class)
            }
        }
    }

    @Test fun `Test getColumnNames`() {
        db.query("""
            SELECT "a" AS string, 2 AS int, 3.0 AS float
            """
        ).use { cursor ->
            assertThat(cursor.columnNames).containsExactly(
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
            assertThat(cursor.getColumnName(0)).isEqualTo("string")
            assertThat(cursor.getColumnName(1)).isEqualTo("int")
            assertThat(cursor.getColumnName(2)).isEqualTo("float")
        }
    }

    @Test fun `Test getCount`() {
        db.query("""
            SELECT * FROM Ships
            WHERE capacity = ?
            """,
            arrayOf(42)
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
        }
    }

    @Test fun `Test getCount for PRAGMA`() {
        db.query("""
            PRAGMA foreign_key_list(`Ships`)
            """
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(0)
        }

        db.query("""
            PRAGMA foreign_key_list(`Pilots`)
            """
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
        }
    }

    @Test fun `Test moveToPosition to -1`() {
        db.insertPilots("Inara", "Mal")

        db.query("""
            SELECT name FROM Pilots
            ORDER BY name ASC
            """
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(3)
            assertThat(cursor.position).isEqualTo(-1)

            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Inara")

            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Mal")

            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Wash")

            assertThat(cursor.moveToPosition(-1)).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Inara")
        }
    }

    @Test fun `moveToPosition to middle`() {
        db.insertPilots("Inara", "Mal")

        db.query("""
            SELECT name FROM Pilots
            ORDER BY name ASC
            """
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(3)

            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Inara")

            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Mal")

            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Wash")

            assertThat(cursor.moveToPosition(1)).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Mal")
        }
    }

    @Test fun `moveToFirst returns true when expected`() {
        db.insertPilots("Inara", "Mal")

        db.query("""
            SELECT name FROM Pilots
            ORDER BY name ASC
            """
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(3)

            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Inara")

            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Wash")

            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Inara")
        }
    }

    @Test fun `moveToFirst returns true as expected with a single entry`() {
        db.query("""
            SELECT name FROM Pilots
            ORDER BY name ASC
            """
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(1)

            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Wash")

            // no more
            assertThat(cursor.moveToNext()).isFalse()

            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(0)).isEqualTo("Wash")
        }
    }

    @Test fun `position should work as expected`() {
        db.insertPilots("Mal", "Inara")

        db.query("""
            SELECT name FROM Pilots
            ORDER BY name ASC
            """
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(3)
            assertThat(cursor.position).isEqualTo(-1)

            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.position).isEqualTo(0)

            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.position).isEqualTo(1)

            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.position).isEqualTo(2)

            assertThat(cursor.moveToNext()).isFalse()
            assertThat(cursor.position).isEqualTo(3)
        }
    }

    @Test fun `position should work as expected with a single value`() {
        db.query("""
            SELECT name FROM Pilots
            ORDER BY name ASC
            """
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            assertThat(cursor.position).isEqualTo(-1)

            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.position).isEqualTo(0)

            // no more
            assertThat(cursor.moveToNext()).isFalse()
            assertThat(cursor.position).isEqualTo(1)
        }
    }

    @Test fun `Handle empty cursor correctly`() {
        db.query("""
            SELECT name FROM Pilots
            WHERE name = "Jayne"
            """
        ).use { cursor ->
            assertThat(cursor.count).isEqualTo(0)
            assertThat(cursor.position).isEqualTo(-1)

            assertThat(cursor.moveToFirst()).isFalse()
            assertThat(cursor.position).isEqualTo(-1)

            assertThat(cursor.moveToNext()).isFalse()
            assertThat(cursor.position).isEqualTo(-1)
        }
    }


}

private fun SupportSQLiteDatabase.insertPilots(vararg names: String) {
    val values = names.map { name -> """
        ("$name", (SELECT id FROM Ships WHERE name = "Serenity"))
    """.trimIndent() }

    execSQL("""
        INSERT INTO Pilots (name, ship)
        VALUES ${values.joinToString(", ")}
    """)
}
