package net.dhleong.sqlead

import android.arch.persistence.db.SupportSQLiteOpenHelper

/**
 * An implementation of [SupportSQLiteOpenHelper.Factory] that uses the version of
 * SQLite available on the host Desktop computer. You may optionally pass `false`
 * for [inMemory] to actually create the database on disk.
 *
 * @author dhleong
 */
class SQLeadSQLiteOpenHelperFactory(
    private val inMemory: Boolean = true
) : SupportSQLiteOpenHelper.Factory {
    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        return SQLeadOpenHelper(
            configuration,
            inMemory = inMemory
        )
    }
}

