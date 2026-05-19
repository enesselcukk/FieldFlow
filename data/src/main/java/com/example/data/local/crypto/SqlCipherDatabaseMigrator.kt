package com.example.data.local.crypto

import android.content.Context
import android.database.SQLException
import android.util.Log
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.io.File

internal object SqlCipherDatabaseMigrator {

    fun ensureNativeLibraryLoaded() {
        System.loadLibrary("sqlcipher")
    }

    private const val TAG = "SqlCipherMigrator"
    private const val DB_NAME = "fieldflow.db"
    private const val BACKUP_SUFFIX = ".migrate_plain_backup"

    fun wipeRoomDatabase(context: Context) {
        context.deleteDatabase(DB_NAME)
        val stale = File(context.getDatabasePath(DB_NAME).parentFile ?: return, "$DB_NAME$BACKUP_SUFFIX")
        deleteQuietly(stale)
        deleteWalArtifacts(stale)
        val base = context.getDatabasePath(DB_NAME)
        deleteWalArtifacts(base)
        deleteQuietly(base)
    }

    fun migratePlainDatabaseIfNeeded(context: Context, passphrase: String) {
        ensureNativeLibraryLoaded()
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) return

        val parent = dbFile.parentFile
        if (parent == null) {
            Log.e(TAG, "Database path has no parent; skipping migration")
            return
        }

        if (canOpenWithPassphrase(dbFile, passphrase)) return

        checkpointAndRemoveWalFiles(dbFile)

        if (!canOpenPlainText(dbFile)) {
            Log.w(TAG, "DB is neither plaintext nor opens with current passphrase; not migrating")
            return
        }

        val backup = File(parent, "$DB_NAME$BACKUP_SUFFIX")
        if (backup.exists() && !backup.delete()) {
            Log.e(TAG, "Could not remove stale migration backup")
            return
        }

        if (!dbFile.renameTo(backup)) {
            Log.e(TAG, "Cannot rename plain DB for encryption migration; plain DB left in place")
            return
        }

        val keySql = passphrase.replace("'", "''")
        val encPath = dbFile.absolutePath.replace("'", "''")

        var plainDb: SQLiteDatabase? = null
        try {
            plainDb = SQLiteDatabase.openDatabase(
                backup.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE,
                null
            )
            plainDb.rawExecSQL("ATTACH DATABASE '$encPath' AS encrypted KEY '$keySql'")
            plainDb.rawExecSQL("SELECT sqlcipher_export('encrypted')")
            plainDb.rawExecSQL("DETACH DATABASE encrypted")
            plainDb.close()
            plainDb = null
        } catch (e: Exception) {
            try {
                plainDb?.close()
            } catch (_: Exception) {
            }
            Log.e(TAG, "Encryption export failed; removing DB files so a new encrypted DB can be created", e)
            wipeMigrationArtifacts(parent, backup)
            throw SQLException("SQLCipher migration failed; local database was reset", e)
        }

        deleteQuietly(backup)
        deleteWalArtifacts(backup)
        Log.i(TAG, "Plaintext database encrypted successfully")
    }

    private fun wipeMigrationArtifacts(parent: File, backup: File) {
        val dbFile = File(parent, DB_NAME)
        deleteQuietly(dbFile)
        deleteQuietly(backup)
        deleteWalArtifacts(dbFile)
        deleteWalArtifacts(backup)
    }

    private fun canOpenWithPassphrase(dbFile: File, passphrase: String): Boolean =
        try {
            val db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                passphrase,
                null,
                SQLiteDatabase.OPEN_READONLY,
                null
            )
            db.close()
            true
        } catch (_: Exception) {
            false
        }

    private fun canOpenPlainText(dbFile: File): Boolean =
        try {
            val db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY,
                null
            )
            db.close()
            true
        } catch (_: Exception) {
            false
        }

    private fun checkpointAndRemoveWalFiles(dbFile: File) {
        try {
            android.database.sqlite.SQLiteDatabase.openDatabase(
                dbFile.path,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
            ).use { db ->
                db.rawQuery("PRAGMA wal_checkpoint(FULL)", null).close()
            }
        } catch (_: Exception) {
        }
        deleteWalArtifacts(dbFile)
    }

    private fun deleteWalArtifacts(base: File) {
        val p = base.parentFile ?: return
        deleteQuietly(File(p, "${base.name}-wal"))
        deleteQuietly(File(p, "${base.name}-shm"))
    }

    private fun deleteQuietly(file: File) {
        try {
            if (file.exists()) file.delete()
        } catch (_: Exception) {
        }
    }
}
