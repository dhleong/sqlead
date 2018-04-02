# sqlead

*A somewhat heavier version of sqlite*

## What?

Sqlead is a somewhat-complete implementation of [SupportSQLiteDatabase][1]
backed by JDBC, so it can be used on a desktop (but cannot be used on
any mobile phone).

## Why?

Partly just for the heck of it, and partly to be able to test [Room DB][2]
queries and migrations without having to spin up an emulator, or connect
to a device.


[1]: https://developer.android.com/reference/android/arch/persistence/db/SupportSQLiteDatabase.html
[2]: https://developer.android.com/topic/libraries/architecture/room.html
