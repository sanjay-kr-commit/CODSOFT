package codsoft.internship.sanjay.todolist

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class TodoListDB(
    context : Context ,
    private val databaseUpdateStatus : MutableState<String?> = mutableStateOf( null )
) : SQLiteOpenHelper(
    context ,
    DATABASE_NAME ,
    null ,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL( TABLE )
    }

    @SuppressLint("Recycle", "Range")
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        databaseUpdateStatus.value = "Updating From version $oldVersion to $newVersion"
        db?.execSQL( TABLE )
        val alteredTableName = "ALTERED_TABLE"
        try {
            when (newVersion) {
                2 -> {
                    databaseUpdateStatus.value = "Creating Temporary Table"
                    db?.execSQL(alteredTableName.AlteredTABLE)
                    databaseUpdateStatus.value = "Migrate Data To New Table"
                    db?.rawQuery("SELECT * FROM ${TodoEntry.TABLE}", arrayOf())?.use { cursor ->
                        val columns = cursor.columnNames
                        while (cursor.moveToNext()) {
                            ContentValues().apply {
                                put( TodoEntry.ID.toString() , System.currentTimeMillis().toString() )
                                columns.forEach { columnName ->
                                    put( columnName , cursor.getString(
                                        cursor.getColumnIndex( columnName )
                                    ) )
                                }
                            }.also { entry ->
                                db.insert(alteredTableName , null , entry)
                            }
                        }
                    }
                    databaseUpdateStatus.value = "Drop Old Table"
                    db?.execSQL( "DROP TABLE IF EXISTS ${TodoEntry.TABLE}" )
                    databaseUpdateStatus.value = "Replace Table"
                    db?.execSQL( "ALTER TABLE $alteredTableName RENAME TO ${TodoEntry.TABLE}" )
                    databaseUpdateStatus.value = "Drop Temporary Table"
                    db?.execSQL( "DROP TABLE IF EXISTS $alteredTableName" )
                }
            }
        } catch ( _ : Exception ) {
            databaseUpdateStatus.value = "FAILED TO UPDATE, DROPPING TABLE"
            db?.execSQL( "DROP TABLE IF EXISTS $alteredTableName" )
            db?.execSQL( "DROP TABLE IF EXISTS ${TodoEntry.TABLE}" )
            onCreate( db )
        }
        databaseUpdateStatus.value = null
    }

    companion object {
        private const val DATABASE_NAME : String = "TODO_LIST.db"
        private const val DATABASE_VERSION : Int = 2
        private val TABLE : String
            get() = TodoEntry.TABLE.toString().AlteredTABLE
        private val String.AlteredTABLE
            get() = """
            CREATE TABLE IF NOT EXISTS ${this}(
                ${TodoEntry.ID} TEXT NOT NULL PRIMARY KEY ,
                ${TodoEntry.TITLE} TEXT ,
                ${TodoEntry.STATUS} TEXT ,
                ${TodoEntry.DESCRIPTION} TEXT
            )
        """.trimIndent()
    }

}