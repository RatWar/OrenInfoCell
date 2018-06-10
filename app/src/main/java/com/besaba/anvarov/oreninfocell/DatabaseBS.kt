package com.besaba.anvarov.oreninfocell

import android.content.Context
import android.database.sqlite.SQLiteQueryBuilder
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

class DatabaseBS(context: Context) : SQLiteAssetHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    fun getBS(bs: String): String {
        val db = readableDatabase
        val qb = SQLiteQueryBuilder()
        qb.tables = "Place"
        val c = qb.query(db, arrayOf("Data"), "CellId = $bs", null, null, null, null)
        c.moveToFirst()
        return when (c.count) {
            0 -> "Not Address"
            else -> c.getString(0)
        }
    }

    companion object {
        private const val DATABASE_NAME = "BS.db"
        private const val DATABASE_VERSION = 1
    }
}