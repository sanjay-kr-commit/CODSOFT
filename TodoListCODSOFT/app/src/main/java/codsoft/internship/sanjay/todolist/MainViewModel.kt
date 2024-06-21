package codsoft.internship.sanjay.todolist

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

// for persistent data between activity reload
data class MainViewModel(
    private var _initialTask_ : Boolean = true ,
    val todoList : MutableList<Map<TodoEntry,MutableState<String?>>> = mutableStateListOf()  ,
) : ViewModel() {

    lateinit var todoListDB : TodoListDB

    fun initialise( initializationTask : MainViewModel.() -> Unit ) {
        if ( _initialTask_ ){
            initializationTask()
            _initialTask_ = false
        }
    }

}