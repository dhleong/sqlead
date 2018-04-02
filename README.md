# sqlead

*A somewhat heavier version of SQLite*

## What?

SQLead is a somewhat-complete implementation of [SupportSQLiteDatabase][1]
backed by JDBC, so it can be used on a desktop (but cannot be used on
any mobile phone).

SQLead is *not* intended to be a particularly efficient or complete
implementation, but merely a *mostly* functional one. Be aware, however,
that it will **probably not** use the version of SQLite that will be available
on the devices your code will eventually run on, so unless you really know
what you're doing, you should probably just run your tests on an emulator,
or on an actual device.

## Why?

Partly to be able to test [Room DB][2] queries and migrations without having
to spin up an emulator, or connect to a device... but given the caveats
mentioned above, mostly just for fun. If you're using the
[Requery SQLite Support Library][3], however, it may not be so crazy.


[1]: https://developer.android.com/reference/android/arch/persistence/db/SupportSQLiteDatabase.html
[2]: https://developer.android.com/topic/libraries/architecture/room.html
[3]: https://github.com/requery/sqlite-android
