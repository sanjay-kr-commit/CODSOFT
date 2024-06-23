package codsoft.internship.sanjay.todolist

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import codsoft.internship.sanjay.todolist.ui.theme.TodoListCODSOFTTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainViewModel by viewModels<MainViewModel>()
        mainViewModel.initialise {
            todoListDB = TodoListDB( this@MainActivity )
            try {
                CoroutineScope( Dispatchers.Main ).launch {
                    todoList.addAll(todoListDB.listTask)
                }
            } catch ( e :Exception ){
                Log.e( "FailedToFetchTasks" , e.stackTraceToString() )
                Toast.makeText( this@MainActivity , "Failed to fetch task", Toast.LENGTH_SHORT ).show()
            }
        }

        val remove : ( Map<TodoEntry,MutableState<String?>> , String ) -> Unit = { entry , title ->
            try {
                mainViewModel.todoListDB.delete( title )
                mainViewModel.todoList.remove( entry )
            } catch ( e : Exception ) {
                Log.e( "FailedToDeleteEntry" ,e.stackTraceToString() )
            }
        }

        // order of argument -> ( TITLE , DESCRIPTION , STATUS )
        val add : ( String,String?,TaskStatus?) -> Unit = { parsedTitle , parsedDescription , parsedStatus ->
            try {
                for ( entry in mainViewModel.todoList ) {
                    if ( entry[TodoEntry.TITLE]!!.value == parsedTitle ) {
                        throw Exception( "Duplicate Entry $parsedTitle" )
                    }
                }
                val status = parsedStatus?.identifier ?: TaskStatus.TODO.identifier
                mainViewModel.todoListDB.insert {
                    put(TodoEntry.TITLE.toString(), parsedTitle)
                    put(TodoEntry.DESCRIPTION.toString(), parsedDescription)
                    put(TodoEntry.STATUS.toString(), status)
                }
                mainViewModel.todoList += mapOf(
                    TodoEntry.TITLE to mutableStateOf( parsedTitle ) ,
                    TodoEntry.STATUS to mutableStateOf( status ) ,
                    TodoEntry.DESCRIPTION to mutableStateOf( parsedDescription )
                )
            } catch ( e : Exception ) {
                Log.e( "FailedToInsertEntry" , e.stackTraceToString() )
            }
        }

        val updateEntry : ( Map<TodoEntry,MutableState<String?>> , String , String? , TaskStatus? ) -> Unit = { entry , parsedTitle , parsedDescription , parsedStatus ->
            try {
                val status = parsedStatus?.identifier ?: TaskStatus.TODO.identifier
                val removeCorrectEntry = entry[TodoEntry.TITLE]?.value ?: parsedTitle
                mainViewModel.todoListDB.delete( removeCorrectEntry )
                mainViewModel.todoListDB.insert {
                    put(TodoEntry.TITLE.toString(), parsedTitle)
                    put(TodoEntry.DESCRIPTION.toString(), parsedDescription)
                    put(TodoEntry.STATUS.toString(), status)
                }
                entry[TodoEntry.TITLE]?.value = parsedTitle
                entry[TodoEntry.STATUS]?.value = status
                entry[TodoEntry.DESCRIPTION]?.value = parsedDescription
            } catch ( e : Exception ) {
                Log.e( "FailedToUpdateEntry" , e.stackTraceToString() )
            }
        }

        enableEdgeToEdge()
        setContent {
            TodoListCODSOFTTheme {
                // pass list as immutable for value can only be changed through lambda
                HomeScreen(
                    listTask = mainViewModel.todoList,
                    add = add ,
                    updateEntry = updateEntry,
                    remove = remove
                )
            }
        }
    }

}