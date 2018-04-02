package net.dhleong.sqlead

import android.app.Instrumentation
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import android.arch.persistence.room.testing.MigrationTestHelper
import android.content.Context
import android.content.res.AssetManager
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File

/**
 * Convenient helper [TestRule] for running Room migration tests
 *  without a real device.
 *  
 * @author dhleong
 */
class MigrationTester(
    dbClass: Class<*>,
    dbDirectory: File = File(".db")
) : TestRule {

    val assets = mock<AssetManager> {
        on { open(any()) } doAnswer {
            File("schemas", it.getArgument<String>(0))
                .inputStream()
        }
    }

    val context = mock<Context> {
        on { assets } doReturn assets
        on { getDatabasePath(any()) } doAnswer {
            File(dbDirectory, it.getArgument<String>(0)).also {
                if (!it.parentFile.exists()) {
                    it.parentFile.mkdirs()
                }
            }
        }
    }

    val instrumentation = mock<Instrumentation> {
        on { context } doReturn context
        on { targetContext } doReturn context
    }

    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation,
        dbClass.canonicalName,
        SQLeadSQLiteOpenHelperFactory(dbDirectory)
    )

    override fun apply(base: Statement?, description: Description?): Statement =
        helper.apply(base, description)

    /**
     * Execute a migration test
     */
    inline fun run(
        migration: Migration,

        /**
         * Called with the old version of the database, so you
         *  can insert some data to later test data persistence.
         *  You cannot use DAO classes, since they expect the
         *  latest schema.
         */
        withOld: (db: SupportSQLiteDatabase) -> Unit = { /* nop by default */ },

        /**
         * MigrationTestHelper automatically verifies schema changes,
         *  but you should validate data migrations
         */
        withNew: (db: SupportSQLiteDatabase) -> Unit = { /* nop by default */ }
    ) {
        val name = "migration-test-${migration.startVersion}-${migration.endVersion}"
        val dbFile = context.getDatabasePath(name).absoluteFile

        // ensure it's not there to start, since the helper will delete it anyway,
        //  but will also warn that they're doing so with Log.d (which may be a stub)
        dbFile.delete()

        helper.createDatabase(name, migration.startVersion)
            .use(withOld)

        helper.runMigrationsAndValidate(
            name,
            migration.endVersion,
            true,
            migration
        ).use(withNew)
    }
}