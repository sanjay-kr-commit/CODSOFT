package codsoft.internship.sanjay.todolist

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TodoListDB( context : Context ) : SQLiteOpenHelper(
    context ,
    DATABASE_NAME ,
    null ,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL( TABLE )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL( "DROP ${TodoEntry.TABLE}" )
        onCreate( db )
    }

    companion object {
        private const val DATABASE_NAME : String = "TODO_LIST.db"
        private const val DATABASE_VERSION : Int = 1
        private val TABLE
            get() = """
            CREATE TABLE IF NOT EXISTS ${TodoEntry.TABLE}(
                ${TodoEntry.TITLE} TEXT NOT NULL PRIMARY KEY ,
                ${TodoEntry.STATUS} TEXT ,
                ${TodoEntry.DESCRIPTION} TEXT
            )
        """.trimIndent()
    }

}