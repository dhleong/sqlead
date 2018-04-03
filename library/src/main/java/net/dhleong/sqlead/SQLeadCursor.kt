package net.dhleong.sqlead

import android.arch.persistence.db.SupportSQLiteQuery
import android.content.ContentResolver
import android.database.CharArrayBuffer
import android.database.ContentObserver
import android.database.Cursor
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle
import java.sql.Connection
import java.sql.ResultSet

internal class SQLeadCursor(
    private val conn: Connection,
    private val sql: String,
    private val query: SupportSQLiteQuery,
    private val results: ResultSet
) : Cursor {

    private var extras = Bundle.EMPTY

    private val myCount by lazy {
        val countQuery = "SELECT COUNT(*) FROM ($sql)"
        SQLeadStatement(conn, countQuery).use {
            query.bindTo(it)
            it.simpleQueryForLong().toInt()
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

    override fun moveToPosition(position: Int): Boolean = results.absolute(position)

    override fun getLong(columnIndex: Int): Long = results.getLong(columnIndex + 1)

    //    override fun moveToFirst(): Boolean = results.absolute(0)
    override fun moveToFirst(): Boolean = !results.isClosed // cannot move backwards with jdbc

    override fun getFloat(columnIndex: Int): Float = results.getFloat(columnIndex + 1)

    override fun moveToPrevious(): Boolean = results.relative(-1)

    override fun getDouble(columnIndex: Int): Double = results.getDouble(columnIndex + 1)

    override fun close() {
        results.close()
    }

    override fun isClosed(): Boolean = results.isClosed

    override fun getCount(): Int = myCount

    override fun isFirst(): Boolean = results.isFirst

    override fun isNull(columnIndex: Int): Boolean = results.getObject(columnIndex + 1) == null

    override fun unregisterContentObserver(observer: ContentObserver?) {
        TODO("not implemented")
    }

    override fun getColumnIndexOrThrow(columnName: String?): Int =
        if (results.isClosed) 0 // it's closed, which means no results anyway
        else results.findColumn(columnName)

    override fun requery(): Boolean {
        TODO("not implemented")
    }

    override fun getWantsAllOnMoveCalls(): Boolean {
        TODO("not implemented")
    }

    override fun getColumnNames(): Array<String> = throw UnsupportedOperationException()

    override fun getInt(columnIndex: Int): Int = results.getInt(columnIndex + 1)

    override fun isLast(): Boolean = results.isLast

    override fun getType(columnIndex: Int): Int = throw UnsupportedOperationException()

    override fun registerDataSetObserver(observer: DataSetObserver?) = throw UnsupportedOperationException()

    override fun moveToNext(): Boolean = results.next()

    override fun getPosition(): Int = results.row

    override fun isBeforeFirst(): Boolean = results.isBeforeFirst

    override fun registerContentObserver(observer: ContentObserver?) = throw UnsupportedOperationException()

    override fun moveToLast(): Boolean = results.last()

    override fun deactivate() = throw UnsupportedOperationException()

    override fun getNotificationUri(): Uri = throw UnsupportedOperationException()

    override fun getColumnName(columnIndex: Int): String = throw UnsupportedOperationException()

    override fun getColumnIndex(columnName: String?): Int = try {
        getColumnIndexOrThrow(columnName)
    } catch (e: Throwable) {
        -1
    }

    override fun getBlob(columnIndex: Int): ByteArray = results.getBytes(columnIndex + 1)

    override fun getShort(columnIndex: Int): Short = results.getShort(columnIndex + 1)

    override fun getString(columnIndex: Int): String? = results.getString(columnIndex + 1)

    override fun move(offset: Int): Boolean = results.relative(offset)

    override fun getColumnCount(): Int = throw UnsupportedOperationException()

    override fun respond(extras: Bundle?): Bundle {
        TODO("not implemented")
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) = throw UnsupportedOperationException()

    override fun isAfterLast(): Boolean = results.isAfterLast

}

