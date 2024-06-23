package codsoft.internship.sanjay.todolist

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            try {
                CoroutineScope( Dispatchers.Main ).launch {
                    todoListDB = TodoListDB( this@MainActivity , mainViewModel.databaseUpdateStatus )
                    todoList.addAll(todoListDB.listTask)
                }
            } catch ( e :Exception ){
                Log.e( "FailedToFetchTasks" , e.stackTraceToString() )
                Toast.makeText( this@MainActivity , "Failed to fetch task", Toast.LENGTH_SHORT ).show()
            }
        }

        val remove : ( Map<TodoEntry,MutableState<String?>> ) -> Unit = { entry->
            try {
                mainViewModel.todoListDB.delete( entry[TodoEntry.ID]!!.value!! )
                mainViewModel.todoList.remove( entry )
            } catch ( e : Exception ) {
                Log.e( "FailedToDeleteEntry" ,e.stackTraceToString() )
            }
        }

        // order of argument -> ( TITLE , DESCRIPTION , STATUS )
        val add : ( String,String?,TaskStatus? ) -> Unit = { parsedTitle , parsedDescription , parsedStatus ->
            try {
                val status = parsedStatus?.identifier ?: TaskStatus.TODO.identifier
                val id = System.currentTimeMillis().toString()
                mainViewModel.todoListDB.insert {
                    put( TodoEntry.ID.toString() , id )
                    put(TodoEntry.TITLE.toString(), parsedTitle)
                    put(TodoEntry.DESCRIPTION.toString(), parsedDescription)
                    put(TodoEntry.STATUS.toString(), status)
                }
                mainViewModel.todoList += mapOf(
                    TodoEntry.ID to mutableStateOf( id ),
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
                mainViewModel.todoListDB.delete( entry[TodoEntry.ID]!!.value!! )
                mainViewModel.todoListDB.insert {
                    put( TodoEntry.ID.toString() , entry[TodoEntry.ID]!!.value!! )
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
                mainViewModel.databaseUpdateStatus.value?.let {
                    Column(modifier = Modifier.fillMaxSize() ,
                        verticalArrangement = Arrangement.Center ,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = it )
                    }
                } ?: run {
                    HomeScreen(
                        listTask = mainViewModel.todoList,
                        add = add,
                        updateEntry = updateEntry,
                        remove = remove
                    )
                }
            }
        }
    }

}