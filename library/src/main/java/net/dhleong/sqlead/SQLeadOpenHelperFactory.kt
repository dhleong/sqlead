package net.dhleong.sqlead

import android.arch.persistence.db.SupportSQLiteOpenHelper
import java.io.File

/**
 * An implementation of [SupportSQLiteOpenHelper.Factory] that uses the version of
 * SQLite available on the host Desktop computer.
 *
 * If you provide [dbDirectory], the database will be created in that folder;
 * otherwise, the database will only exist in memory.
 *
 * @author dhleong
 */
class SQLeadSQLiteOpenHelperFactory(
    private val dbDirectory: File? = null
) : SupportSQLiteOpenHelper.Factory {
    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        return SQLeadOpenHelper(
            configuration,
            dbDirectory = dbDirectory
        )
    }
}

