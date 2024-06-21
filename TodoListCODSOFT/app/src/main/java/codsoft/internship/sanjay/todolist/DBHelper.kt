package codsoft.internship.sanjay.todolist

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import codsoft.internship.sanjay.todolist.TodoEntry.Companion.transform

fun TodoListDB.insert( contentValueBlock : ContentValues.() -> Unit ) {
    val contentValue = ContentValues().apply {
        contentValueBlock()
    }
    writableDatabase.insert( TodoEntry.TABLE.toString() , null , contentValue )
}

fun TodoListDB.delete( title : String ) : Boolean = try {
    val entryDeleted = writableDatabase.delete( TodoEntry.TABLE.toString() , "${TodoEntry.TITLE}=?" , arrayOf( title ) )
    if ( entryDeleted == 0 ) throw Exception(
        "0 Column delete title passed : \"$title\""
    )
    true
} catch ( e : Exception ) {
    Log.e( "FailedDeleteTransaction" , e.stackTraceToString() )
    false
}

val TodoListDB.listTask : ArrayList<Map<TodoEntry,MutableState<String?>>>
@SuppressLint("Range")
get() =  arrayListOf<Map<TodoEntry,MutableState<String?>> >().apply {
    val database = this@listTask.readableDatabase
    database.rawQuery( "SELECT * FROM ${TodoEntry.TABLE}" , arrayOf() ).use { cursor ->
        val attr = cursor.columnNames
        while ( cursor.moveToNext() ) {
            HashMap<TodoEntry,MutableState<String?>>().apply {
                attr.forEach {
                    it?.let {
                        this[ it.transform ] = mutableStateOf(cursor.getString( cursor.getColumnIndex( it ) ) )
                    }
                }
            }.let { add( it ) }
        }

    }
}