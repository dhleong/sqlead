package net.dhleong.sqlead

import android.arch.persistence.db.SimpleSQLiteQuery
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteQuery
import android.arch.persistence.db.SupportSQLiteStatement
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.text.TextUtils.isEmpty
import android.util.Pair
import java.sql.DriverManager
import java.util.Locale

class SQLeadSupportDatabase(
    /**
     * If null, the DB will only exist in memory
     */
    dbPath: String? = null
) : SupportSQLiteDatabase {

    @Suppress("ConstantConditionIf")
    private val conn = DriverManager.getConnection(
        if (dbPath == null) {
            "jdbc:sqlite:"  // in-memory DB
        } else {
            "jdbc:sqlite:$dbPath"  // on disk, if debugging is important
        }
    )
    private var pageSize: Long = 20

    private var version = -1

    override fun setMaximumSize(numBytes: Long): Long = numBytes // ?

    override fun insert(table: String?, conflictAlgorithm: Int, values: ContentValues?): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableWriteAheadLogging(): Boolean = false

    override fun isDatabaseIntegrityOk(): Boolean = true

    override fun isWriteAheadLoggingEnabled(): Boolean = false

    override fun disableWriteAheadLogging() {
    }

    override fun compileStatement(sql: String): SupportSQLiteStatement {
        return SQLeadStatement(conn, sql)
    }

    override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener?) {
        beginTransactionWithListener(transactionListener)
    }

    override fun isDbLockedByCurrentThread(): Boolean {
        TODO("not implemented")
    }

    override fun setPageSize(numBytes: Long) {
        pageSize = numBytes
    }

    override fun query(query: String?): Cursor = query(SimpleSQLiteQuery(query))

    override fun query(query: String?, bindArgs: Array<out Any>?): Cursor =
        query(SimpleSQLiteQuery(query, bindArgs))

    override fun query(query: SupportSQLiteQuery): Cursor = query(query, null)

    override fun query(
        query: SupportSQLiteQuery,
        cancellationSignal: CancellationSignal?
    ): Cursor {
        return SQLeadStatement(conn, query.sql!!).also {
            query.bindTo(it)
        }.query()
    }

    override fun endTransaction() {
        if (conn.autoCommit) {
            // successful txn was already committed; this is a nop
        } else {
            // if we still have autoCommit disabled, the transaction failed
            conn.rollback()
            conn.autoCommit = true
        }
    }

    override fun getMaximumSize(): Long = Long.MAX_VALUE

    override fun setLocale(locale: Locale?) {
        // ?
    }

    override fun beginTransaction() {
        conn.autoCommit = false
    }

    override fun update(
        table: String?,
        conflictAlgorithm: Int,
        values: ContentValues?,
        whereClause: String?,
        whereArgs: Array<out Any>?
    ): Int {
        // taken from SQLiteDatabase class.
        if (values == null || values.size() == 0) {
            throw IllegalArgumentException("Empty values");
        }
        val sql = StringBuilder(120)
        sql.append("UPDATE ")
        sql.append(CONFLICT_VALUES[conflictAlgorithm]);
        sql.append(table)
        sql.append(" SET ")

        // move all bind args to one array
        val setValuesSize = values.size()
        val bindArgsSize =
            if (whereArgs == null) setValuesSize
            else setValuesSize + whereArgs.size
        val bindArgs = arrayOfNulls<Any>(bindArgsSize)
        var i = 0
        for (colName in values.keySet()) {
            sql.append(if (i > 0) "," else "")
            sql.append(colName)
            bindArgs[i++] = values.get(colName)
            sql.append("=?")
        }
        if (whereArgs != null) {
            i = setValuesSize
            while (i < bindArgsSize) {
                bindArgs[i] = whereArgs[i - setValuesSize]
                i++
            }
        }
        if (!isEmpty(whereClause)) {
            sql.append(" WHERE ")
            sql.append(whereClause)
        }
        val stmt = compileStatement(sql.toString())
        SimpleSQLiteQuery.bind(stmt, bindArgs)

        return stmt.executeUpdateDelete();
    }

    override fun isOpen(): Boolean = !conn.isClosed

    override fun getAttachedDbs(): MutableList<Pair<String, String>> {
        TODO("not implemented")
    }

    override fun getVersion(): Int = version

    override fun execSQL(sql: String) {
        compileStatement(sql).use {
            it.execute()
        }
    }

    override fun execSQL(sql: String?, bindArgs: Array<out Any>?) {
        val stmt = SimpleSQLiteQuery(sql, bindArgs)
        SQLeadStatement(conn, sql).use {
            stmt.bindTo(it)
            it.execute()
        }
    }

    override fun yieldIfContendedSafely(): Boolean {
        TODO("not implemented")
    }

    override fun yieldIfContendedSafely(sleepAfterYieldDelay: Long): Boolean {
        TODO("not implemented")
    }

    override fun close() {
        conn.close()
    }

    override fun delete(table: String?, whereClause: String?, whereArgs: Array<out Any>?): Int {
        val sql = if (whereClause != null) {
            "DELETE FROM $table WHERE $whereClause"
        } else {
            "DELETE FROM $table"
        }

        return SQLeadStatement(conn, sql).also {
            SimpleSQLiteQuery(sql, whereArgs).bindTo(it)
        }.executeUpdateDelete()
    }

    override fun needUpgrade(newVersion: Int): Boolean = false

    override fun setMaxSqlCacheSize(cacheSize: Int) {
        // ?
    }

    override fun setForeignKeyConstraintsEnabled(enable: Boolean) {
        // ?
    }

    override fun beginTransactionNonExclusive() {
        conn.autoCommit = false
    }

    override fun setTransactionSuccessful() {
        conn.autoCommit = true
    }

    override fun setVersion(version: Int) {
        this.version = version
    }

    override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener?) {
        conn.autoCommit = false
//        transactionListener?.onBegin()
    }

    override fun inTransaction(): Boolean = conn.autoCommit

    override fun isReadOnly(): Boolean = false

    override fun getPath(): String = ""

    override fun getPageSize(): Long = pageSize

    companion object {
        private val CONFLICT_VALUES =
            arrayOf("", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE ")
    }
}
