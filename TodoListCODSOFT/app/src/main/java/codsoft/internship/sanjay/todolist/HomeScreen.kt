package codsoft.internship.sanjay.todolist

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import codsoft.internship.sanjay.todolist.TaskStatus.Companion.taskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    listTask : MutableList<Map<TodoEntry,MutableState<String?>>> ,
    // order of argument -> ( TITLE , DESCRIPTION , STATUS )
    add : ( String , String? , TaskStatus? ) -> Unit ,
    updateEntry : ( Map<TodoEntry,MutableState<String?>> , String , String? , TaskStatus? ) -> Unit ,
    remove : ( Map<TodoEntry,MutableState<String?>> , String ) -> Unit
){

    val isCreationTabVisible = remember {
        mutableStateOf( false )
    }

    val titleField = remember {
        mutableStateOf("")
    }
    val statusFiled = remember {
        mutableStateOf(TaskStatus.TODO.toString())
    }
    val descriptionFiled = remember {
        mutableStateOf("")
    }
    val editEntry : MutableState<Map<TodoEntry,MutableState<String?>>?> = remember {
        mutableStateOf( null )
    }

    Scaffold(
        Modifier
            .fillMaxSize()
            .padding(10.dp)
            .clip(RoundedCornerShape(10.dp)) ,
        bottomBar = {
            FloatingButton( isCreationTabVisible )
        }
    ) {
        it.calculateTopPadding()
        TaskList(
            listTask ,
            updateEntry ,
            remove ,
            isCreationTabVisible ,
            editEntry ,
            titleField ,
            descriptionFiled ,
            statusFiled
        )
        EditMenu(
            isCreationTabVisible ,
            editEntry ,
            titleField ,
            descriptionFiled ,
            statusFiled ,
            add ,
            updateEntry
        )
    }

}

@Composable
private fun TaskList(
    listTask : MutableList<Map<TodoEntry,MutableState<String?>>> ,
    updateEntry : ( Map<TodoEntry,MutableState<String?>> , String , String? , TaskStatus? ) -> Unit ,
    remove : ( Map<TodoEntry,MutableState<String?>> ,String ) -> Unit ,
    isCreationTabVisible: MutableState<Boolean>,
    editEntry : MutableState<Map<TodoEntry,MutableState<String?>>?> ,
    titleField : MutableState<String> ,
    descriptionFiled : MutableState<String> ,
    statusFiled : MutableState<String> ,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        listTask.forEach { entry ->
            val showDescription = mutableStateOf(false)
            item {
                AnimatedVisibility(
                    visible = true ,
                    enter = fadeIn()
                ) {
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .animateContentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Gray)
                            .padding(10.dp)
                    ) {

                        TaskListTitleBox(entry , updateEntry, showDescription )
                        TaskListDescriptionBox(
                            showDescription,
                            entry,
                            editEntry,
                            titleField,
                            descriptionFiled,
                            statusFiled,
                            isCreationTabVisible,
                            remove,
                            updateEntry
                        )

                    }
                }
            }
        }
    }
}

@Composable
private fun EditMenu(
    isCreationTabVisible: MutableState<Boolean>,
    editEntry : MutableState<Map<TodoEntry,MutableState<String?>>?>  ,
    titleField : MutableState<String> ,
    descriptionFiled : MutableState<String> ,
    statusFiled : MutableState<String> ,
    add : ( String , String? , TaskStatus? ) -> Unit ,
    updateEntry : ( Map<TodoEntry,MutableState<String?>> ,String , String? , TaskStatus? ) -> Unit
) {
    AnimatedVisibility(
        visible = isCreationTabVisible.value ,
        enter = fadeIn() ,
        exit = fadeOut() ,
    ) {
        LazyColumn ( modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .clip(RoundedCornerShape(10.dp)) ,
            verticalArrangement = Arrangement.Bottom ,
            horizontalAlignment = Alignment.Start
        ) {
            item {
                EditMenuTitleField(titleField)
            }
            item {
                EditMenuDescriptionField(descriptionFiled)
            }
            item {
                EditMenuStatusButton(statusFiled)
            }
            item {
                EditMenuApplyButton(
                    isCreationTabVisible ,
                    editEntry ,
                    titleField ,
                    descriptionFiled ,
                    statusFiled ,
                    add ,
                    updateEntry
                )
            }
        }
        BackHandler( isCreationTabVisible.value ) {
            isCreationTabVisible.value = false
            titleField.value = ""
            descriptionFiled.value = ""
            statusFiled.value = TaskStatus.TODO.toString()
            editEntry.value = null
        }
    }
}

@Composable
private fun FloatingButton(
    isCreationTabVisible : MutableState<Boolean>
){
    AnimatedVisibility(
        visible = !isCreationTabVisible.value ,
        enter = slideIn( initialOffset = { IntOffset( it.height , it.width ) } ) ,
        exit = slideOut( targetOffset = { IntOffset( it.height , it.width ) } ) ,
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
            ,
            verticalAlignment = Alignment.CenterVertically ,
            horizontalArrangement = Arrangement.End
        ){
            Icon(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable(!isCreationTabVisible.value, onClick = {
                        isCreationTabVisible.value = true
                    })
                    .padding(15.dp)
                ,
                imageVector = Icons.Filled.Add ,
                contentDescription = "" )
        }
    }
}

@Composable
private fun TaskListTitleBox(
    entry : Map<TodoEntry,MutableState<String?>>,
    updateEntry: ( Map<TodoEntry,MutableState<String?>> ,String, String?, TaskStatus?) -> Unit,
    showDescription: MutableState<Boolean>
) {
    Row (
        modifier = Modifier.fillMaxWidth() ,
        verticalAlignment = Alignment.CenterVertically ,
        horizontalArrangement = Arrangement.Start
    ) {
        ListTaskStatusIndicator(entry , updateEntry )
        Spacer(modifier = Modifier.padding( horizontal = 5.dp ) )
        ListTaskTitle(entry , showDescription )
    }
}

@Composable
private fun TaskListDescriptionBox(
    showDescription: MutableState<Boolean>,
    entry: Map<TodoEntry, MutableState<String?>>,
    editEntry : MutableState<Map<TodoEntry,MutableState<String?>>?> ,
    titleField: MutableState<String>,
    descriptionFiled: MutableState<String>,
    statusFiled: MutableState<String>,
    isCreationTabVisible: MutableState<Boolean>,
    remove: (Map<TodoEntry,MutableState<String?>> ,String) -> Unit,
    updateEntry: (Map<TodoEntry,MutableState<String?>> ,String, String?, TaskStatus?) -> Unit
) {
    if ( showDescription.value ) {
        val editMode = remember {
            mutableStateOf( false )
        }
        if ( editMode.value ) ListTaskDescriptionBoxEditMode(
            entry ,
            updateEntry ,
            editMode
        )
        else ListTaskDescriptionBoxCardView(
            entry ,
            editMode ,
            editEntry ,
            titleField ,
            descriptionFiled ,
            statusFiled ,
            isCreationTabVisible ,
            remove
        )
    }
}

@Composable
private fun ListTaskDescriptionBoxEditMode(
    entry: Map<TodoEntry, MutableState<String?>> ,
    updateEntry: (Map<TodoEntry,MutableState<String?>> ,String, String?, TaskStatus?) -> Unit ,
    editMode : MutableState<Boolean>
) {
    var editField by remember {
        mutableStateOf( "${entry[TodoEntry.DESCRIPTION]!!.value}" )
    }
    TextField(value = editField , onValueChange ={
        editField = it
    } , modifier = Modifier.fillMaxWidth() )
    Spacer(modifier = Modifier.height( 5.dp ))
    Button(onClick = {
        updateEntry(
            entry ,
            entry[TodoEntry.TITLE]!!.value!!,
            editField,
            entry[TodoEntry.STATUS]!!.value!!.taskStatus
        )
        editMode.value = false
    }) {
        Text(text = "Apply" )
    }
    BackHandler ( editMode.value ) {
        editMode.value = false
    }
}

@Composable
private fun ListTaskDescriptionBoxCardView(
    entry: Map<TodoEntry, MutableState<String?>>,
    editMode: MutableState<Boolean>,
    editEntry : MutableState<Map<TodoEntry,MutableState<String?>>?> ,
    titleField: MutableState<String>,
    descriptionFiled: MutableState<String>,
    statusFiled: MutableState<String>,
    isCreationTabVisible: MutableState<Boolean>,
    remove: (Map<TodoEntry,MutableState<String?>> ,String) -> Unit
) {
    ListTaskDescriptionBoxDescription(entry , editMode )
    ListTaskDescriptionBoxButtons(
        entry ,
        editEntry ,
        titleField ,
        descriptionFiled ,
        statusFiled ,
        isCreationTabVisible ,
        remove
    )
}

@Composable
private fun ListTaskDescriptionBoxDescription(
    entry: Map<TodoEntry, MutableState<String?>>,
    editMode: MutableState<Boolean>
) {
    Text(text = "${entry[TodoEntry.DESCRIPTION]!!.value}" ,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {
                editMode.value = !editMode.value
            }
    )
}

@Composable
private fun ListTaskDescriptionBoxButtons(
    entry: Map<TodoEntry, MutableState<String?>>,
    editEntry : MutableState<Map<TodoEntry,MutableState<String?>>?> ,
    titleField: MutableState<String>,
    descriptionFiled: MutableState<String>,
    statusFiled: MutableState<String>,
    isCreationTabVisible: MutableState<Boolean>,
    remove: (Map<TodoEntry,MutableState<String?>> ,String) -> Unit
) {

    ListTaskDescriptionBoxRemoveEntryButton(entry , remove)
    ListTaskDescriptionBoxEditEntryButton(
        entry ,
        editEntry ,
        titleField ,
        descriptionFiled ,
        statusFiled ,
        isCreationTabVisible
    )
}

@Composable
private fun ListTaskDescriptionBoxRemoveEntryButton(
    entry: Map<TodoEntry, MutableState<String?>>,
    remove: (Map<TodoEntry,MutableState<String?>> ,String) -> Unit
) {
    Button(onClick = {
        remove( entry, entry[TodoEntry.TITLE]!!.value.toString() )
    }) {
        Row (
            modifier = Modifier.fillMaxWidth() ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon( Icons.Filled.Clear , contentDescription = "remove" )
            Spacer(modifier =  Modifier.padding( horizontal = 5.dp ))
            Text(text =  "Remove Entry" )
        }
    }
}

@Composable
private fun ListTaskDescriptionBoxEditEntryButton(
    entry: Map<TodoEntry, MutableState<String?>>,
    editEntry : MutableState<Map<TodoEntry,MutableState<String?>>?> ,
    titleField: MutableState<String>,
    descriptionFiled: MutableState<String>,
    statusFiled: MutableState<String>,
    isCreationTabVisible: MutableState<Boolean>
) {
    Button(onClick = {
        editEntry.value = entry
        titleField.value = entry[TodoEntry.TITLE]!!.value!!
        descriptionFiled.value = entry[TodoEntry.DESCRIPTION]!!.value!!
        statusFiled.value = entry[TodoEntry.STATUS]!!.value!!
        isCreationTabVisible.value = true
    }) {
        Row (
            modifier = Modifier.fillMaxWidth() ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon( Icons.Filled.Edit , contentDescription = "remove" )
            Spacer(modifier =  Modifier.padding( horizontal = 5.dp ))
            Text(text =  "Edit Entry" )
        }
    }

}

@Composable
private fun RowScope.ListTaskTitle(
    entry : Map<TodoEntry,MutableState<String?>> ,
    showDescription : MutableState<Boolean>
) {
    Box( modifier = Modifier
        .weight(9f)
        .fillMaxWidth()
        .clickable(true, onClick = {
            showDescription.value = !showDescription.value
        }) ) {
        Text(text = "${entry[TodoEntry.TITLE]!!.value}")
    }
}

@Composable
private fun RowScope.ListTaskStatusIndicator(
    entry : Map<TodoEntry,MutableState<String?>> ,
    updateEntry : ( Map<TodoEntry,MutableState<String?>> ,String , String? , TaskStatus? ) -> Unit
) {
    Icon(
        imageVector = entry[TodoEntry.STATUS]!!.value!!.taskStatus.icon
        , contentDescription = "status" ,
        modifier = Modifier
            .clickable(true, onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    updateEntry(
                        entry,
                        entry[TodoEntry.TITLE]!!.value!!,
                        entry[TodoEntry.DESCRIPTION]!!.value,
                        when (entry[TodoEntry.STATUS]!!.value!!.taskStatus.identifier) {
                            TaskStatus.COMPLETED.identifier -> TaskStatus.TODO
                            TaskStatus.TODO.identifier -> TaskStatus.COMPLETED
                            else -> TaskStatus.TODO
                        }
                    )
                    Log.d(
                        "Icon",
                        entry[TodoEntry.STATUS]!!.value!!
                    )
                }
            })
            .padding(5.dp)
            .weight(1f)
    )
}

@Composable
private fun EditMenuTitleField(
    titleField : MutableState<String>
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp)
    ) {
        TextField(value = titleField.value, onValueChange = { titleField.value = it } ,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true ,
            label = {
                Text(text = "Title" )
            }
        )
    }
}

@Composable
private fun EditMenuDescriptionField(
    descriptionFiled : MutableState<String>
) {
    TextField(value = descriptionFiled.value, onValueChange = { descriptionFiled.value = it }, label = {
        Text(text = "Description")
    } , modifier = Modifier
        .padding(vertical = 10.dp)
        .fillMaxWidth()
    )
}

@Composable
private fun EditMenuStatusButton(
    statusFiled : MutableState<String>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp) ,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Text(text = "Status : ")
        for (entry in TaskStatus.entries) {
            if ( entry == TaskStatus.UNKNOWN ) continue
            Button(onClick = {
                statusFiled.value = entry.toString()
            }) {
                Icon(
                    entry.icon
                    , "" ,
                    tint = if ( statusFiled.value == entry.identifier ) Color.DarkGray else LocalContentColor.current ,
                    modifier = Modifier
                        .padding( horizontal = 2.dp )
                )
            }
            Spacer(modifier = Modifier.width( 5.dp ))

        }
    }
}

@Composable
private fun EditMenuApplyButton(
    isCreationTabVisible: MutableState<Boolean>,
    editEntry: MutableState<Map<TodoEntry, MutableState<String?>>?>,
    titleField : MutableState<String>,
    descriptionFiled : MutableState<String>,
    statusFiled : MutableState<String>,
    add : ( String , String? , TaskStatus? ) -> Unit,
    updateEntry : ( Map<TodoEntry,MutableState<String?>> ,String , String? , TaskStatus? ) -> Unit
) {

    val isUpdating by remember {
        mutableStateOf( editEntry.value != null )
    }

    var buttonTitle by remember {
        mutableStateOf( if ( isUpdating ) "Update" else "Add Entry" )
    }
    Row ( modifier = Modifier.fillMaxWidth() ,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = {
            CoroutineScope( Dispatchers.Main ).launch {
                if ( titleField.value.isBlank() ) {
                    buttonTitle = "Title cannot be empty"
                    delay( 1000 )
                    buttonTitle = if ( isUpdating ) "Update" else "Add Entry"
                } else {
                    if ( !isUpdating ) add( titleField.value , descriptionFiled.value , statusFiled.value.taskStatus )
                    else updateEntry( editEntry.value!! , titleField.value, descriptionFiled.value , statusFiled.value.taskStatus )
                }
            }
        } , modifier = Modifier
            .weight(1f)
            .padding(horizontal = 5.dp) ) {
            Text(text = buttonTitle  )
        }

        Button(onClick = {
            isCreationTabVisible.value = false
            titleField.value = ""
            descriptionFiled.value = ""
            statusFiled.value = TaskStatus.TODO.toString()
            editEntry.value = null
        } , modifier = Modifier
            .weight(1f)
            .padding(horizontal = 5.dp)) {
            Text(text = "Close"  )
        }
    }
}