package net.dhleong.sqlead

import android.content.ContentResolver
import android.database.CharArrayBuffer
import android.database.ContentObserver
import android.database.Cursor
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle
import androidx.sqlite.db.SupportSQLiteQuery
import org.sqlite.core.CoreResultSet
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Types

internal class SQLeadCursor(
    private val conn: Connection,
    private val sql: String,
    private val query: SupportSQLiteQuery,
    private val statement: PreparedStatement
) : Cursor {

    private var extras = Bundle.EMPTY

    private var results: ResultSet = statement.executeQuery()

    private val myCount by lazy {
        if (!sql.trim().startsWith("PRAGMA", ignoreCase = true)) {
            val countQuery = "SELECT COUNT(*) FROM ($sql)"
            SQLeadStatement(conn, countQuery).use {
                query.bindTo(it)
                it.simpleQueryForLong().toInt()
            }
        } else {
            // for pragma queries we have to just execute it and count by hand
            SQLeadStatement(conn, sql).use {
                query.bindTo(it)
                (it.query(query) as SQLeadCursor).use { cursor ->
                    // NOTE unfortunately, result.last() is not implemented by jdbc,
                    // so we have to roll there by hand
                    var count = 0
                    while (cursor.moveToNext()) {
                        ++count
                    }
                    count
                }
            }
        }
    }

    override fun setNotificationUri(cr: ContentResolver?, uri: Uri?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer) {
        val str = getString(columnIndex)
        if (str != null) {
            str.toCharArray(buffer.data, 0, 0, str.length)
            buffer.sizeCopied = str.length
        }
    }

    override fun getExtras(): Bundle = extras

    override fun setExtras(extras: Bundle?) {
        this.extras = extras ?: Bundle.EMPTY
    }

    override fun moveToPosition(position: Int): Boolean {
        if (position < this.position) {
            // NOTE: jdbc sqlite for some reason only supports FORWARD_ONLY,
            // so if we need to rewind, we close and re-query
            results.close()
            results = statement.executeQuery()
        }

        // another bummer about FORWARD_ONLY is that absolute() doesn't work,
        // so we have to move one by one
        for (i in 0 until (position - this.position)) {
            if (!results.next()) return false
        }

        return true
    }

    override fun getLong(columnIndex: Int): Long = results.getLong(columnIndex + 1)

    override fun moveToFirst(): Boolean = moveToPosition(0)

    override fun getFloat(columnIndex: Int): Float = results.getFloat(columnIndex + 1)

    override fun moveToPrevious(): Boolean = moveToPosition(position - 1)

    override fun getDouble(columnIndex: Int): Double = results.getDouble(columnIndex + 1)

    override fun close() {
        results.close()
    }

    override fun isClosed(): Boolean = results.isClosed

    override fun getCount(): Int = myCount

    override fun isFirst(): Boolean = results.isFirst

    override fun isNull(columnIndex: Int): Boolean = getType(columnIndex) == Cursor.FIELD_TYPE_NULL

    override fun unregisterContentObserver(observer: ContentObserver?) {
        TODO("not implemented")
    }

    override fun getColumnIndexOrThrow(columnName: String?): Int = try {
        if (results.isClosed) 0 // it's closed, which means no results anyway
        else results.findColumn(columnName) - 1  // 1-based -> 0-based
    } catch (e: SQLException) {
        throw IllegalArgumentException(e)
    }

    // we have to implement the interface, but it's deprecated so we won't bother
    // to try to support the functionality
    @Suppress("OverridingDeprecatedMember")
    override fun requery(): Boolean = throw UnsupportedOperationException()

    override fun getWantsAllOnMoveCalls(): Boolean {
        TODO("not implemented")
    }

    override fun getColumnNames(): Array<String> = (results as CoreResultSet).cols

    override fun getInt(columnIndex: Int): Int = results.getInt(columnIndex + 1)

    override fun isLast(): Boolean = results.isLast

    override fun getType(columnIndex: Int): Int =
        when ((results as ResultSetMetaData).getColumnType(columnIndex + 1)) {
            Types.ARRAY,
            Types.BLOB,
            Types.BINARY -> Cursor.FIELD_TYPE_BLOB

            Types.VARCHAR,
            Types.CHAR -> Cursor.FIELD_TYPE_STRING

            Types.DECIMAL,
            Types.DOUBLE,
            Types.NUMERIC,
            Types.REAL,
            Types.FLOAT -> Cursor.FIELD_TYPE_FLOAT

            Types.BOOLEAN,
            Types.TINYINT,
            Types.SMALLINT,
            Types.BIGINT,
            Types.DATE,
            Types.TIMESTAMP,
            Types.INTEGER -> Cursor.FIELD_TYPE_INTEGER

            Types.NULL -> Cursor.FIELD_TYPE_NULL

            else -> throw IllegalStateException("Unknown type for column $columnIndex")
        }

    override fun registerDataSetObserver(observer: DataSetObserver?) = throw UnsupportedOperationException()

    override fun moveToNext(): Boolean = results.next()

    override fun getPosition(): Int = results.row - 1

    override fun isBeforeFirst(): Boolean = results.isBeforeFirst

    override fun registerContentObserver(observer: ContentObserver?) = throw UnsupportedOperationException()

    override fun moveToLast(): Boolean = results.last()

    // we have to implement the interface, but it's deprecated so we won't bother
    // to try to support the functionality
    @Suppress("OverridingDeprecatedMember")
    override fun deactivate() = throw UnsupportedOperationException()

    override fun getNotificationUri(): Uri = throw UnsupportedOperationException()

    override fun getColumnName(columnIndex: Int): String = columnNames[columnIndex]

    override fun getColumnIndex(columnName: String?): Int = try {
        getColumnIndexOrThrow(columnName)
    } catch (e: IllegalArgumentException) {
        -1
    }

    override fun getBlob(columnIndex: Int): ByteArray = results.getBytes(columnIndex + 1)

    override fun getShort(columnIndex: Int): Short = results.getShort(columnIndex + 1)

    override fun getString(columnIndex: Int): String? = results.getString(columnIndex + 1)

    override fun move(offset: Int): Boolean = results.relative(offset)

    override fun getColumnCount(): Int = columnNames.size

    override fun respond(extras: Bundle?): Bundle {
        TODO("not implemented")
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) = throw UnsupportedOperationException()

    override fun isAfterLast(): Boolean = results.isAfterLast

}

