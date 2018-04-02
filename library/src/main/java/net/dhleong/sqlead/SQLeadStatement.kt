package net.dhleong.sqlead

import android.arch.persistence.db.SupportSQLiteStatement
import android.database.Cursor
import java.sql.Connection

internal class SQLeadStatement(
    private val conn: Connection,
    private val sql: String
) : SupportSQLiteStatement {

    private val stmt = conn.prepareStatement(sql)

    override fun bindLong(index: Int, value: Long) {
        stmt.setLong(index, value)
    }

    override fun simpleQueryForLong(): Long = stmt.executeQuery().getLong(0)

    override fun bindString(index: Int, value: String?) {
        stmt.setString(index, value)
    }

    override fun bindDouble(index: Int, value: Double) {
        stmt.setDouble(index, value)
    }

    override fun simpleQueryForString(): String = stmt.executeQuery().getString(0)

    override fun clearBindings() {
        stmt.clearParameters()
    }

    override fun execute() {
        stmt.execute()
    }

    override fun executeInsert(): Long = stmt.executeUpdate().toLong()

    override fun bindBlob(index: Int, value: ByteArray?) {
        stmt.setBlob(index, value?.inputStream())
    }

    override fun executeUpdateDelete(): Int = stmt.executeUpdate()

    override fun close() {
        stmt.close()
    }

    override fun bindNull(index: Int) {
        stmt.setString(index, null)
    }

    fun query(): Cursor = SQLeadCursor(
        conn,
        sql,
        stmt.executeQuery()
    )

}

