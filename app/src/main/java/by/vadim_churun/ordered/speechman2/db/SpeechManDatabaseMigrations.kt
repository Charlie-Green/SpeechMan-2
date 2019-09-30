package by.vadim_churun.ordered.speechman2.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


/** Produces [Migration] objects for the application database. **/
internal object SpeechManDatabaseMigrations
{
    /** Application versions. **/
    enum class Version {
        V2_0_ALPHA1,
        V2_0_BETA1,
        V2_1_ALPHA1
    }

    fun get(from: Version, to: Version): Migration
    {
        if(from == Version.V2_0_ALPHA1 && to == Version.V2_0_BETA1) {
            return object: Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase)
                { /* Data model didn't change. */ }
            }
        }

        if(from == Version.V2_0_BETA1 && to == Version.V2_1_ALPHA1) {
            return object: Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase)
                {
                    database.execSQL("alter table Orders add paid text not null")
                    database.execSQL("alter table Products add isDeleted integer not null")
                }
            }
        }

        throw IllegalArgumentException(
            "Direct migration from ${from.name} to ${to.name} is not supported." )
    }
}