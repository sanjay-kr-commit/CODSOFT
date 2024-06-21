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
    updateEntry : ( String , String? , TaskStatus? ) -> Unit ,
    remove : ( String ) -> Unit
){

    var isCreationTabVisible by remember {
        mutableStateOf( false )
    }
    var isUpdating by remember {
        mutableStateOf( false )
    }
    var titleField by remember {
        mutableStateOf("")
    }
    var statusFiled by remember {
        mutableStateOf(TaskStatus.TODO.toString())
    }
    var descriptionFiled by remember {
        mutableStateOf("")
    }


    Scaffold(
        Modifier
            .fillMaxSize()
            .padding(10.dp)
            .clip(RoundedCornerShape(10.dp)) ,
        bottomBar = {
            AnimatedVisibility(
                visible = !isCreationTabVisible ,
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
                            .clickable(!isCreationTabVisible, onClick = {
                                isCreationTabVisible = true
                            })
                            .padding(15.dp)
                        ,
                        imageVector = Icons.Filled.Add ,
                        contentDescription = "" )
                }
            }
        }
    ) { it ->

        it.calculateTopPadding()
        // List View
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            listTask.forEach { entry ->
                var showDescription by mutableStateOf(false)
                item {
                    AnimatedVisibility(visible = true , enter = fadeIn() ) {
                        Column (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .animateContentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Gray)
                                .padding(10.dp)
                        ) {

                            Row (
                                modifier = Modifier.fillMaxWidth() ,
                                verticalAlignment = Alignment.CenterVertically ,
                                horizontalArrangement = Arrangement.Start
                            ) {

                                // status icon
                                Icon(
                                    imageVector = entry[TodoEntry.STATUS]!!.value!!.taskStatus.icon
                                    , contentDescription = "status" ,
                                    modifier = Modifier
                                        .clickable(true, onClick = {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                updateEntry(
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
                                // title
                                Spacer(modifier = Modifier.padding( horizontal = 5.dp ) )
                                Box( modifier = Modifier
                                    .weight(9f)
                                    .fillMaxWidth()
                                    .clickable(true, onClick = {
                                        showDescription = !showDescription
                                    }) ) {
                                    Text(text = "${entry[TodoEntry.TITLE]!!.value}")
                                }
                            }

                            // description
                            if ( showDescription ) {
                                var editMode by remember {
                                    mutableStateOf( false )
                                }
                                if ( ! editMode ){
                                    Text(text = "${entry[TodoEntry.DESCRIPTION]!!.value}" ,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .clickable {
                                                editMode = !editMode
                                            }
                                    )
                                    Button(onClick = {
                                        remove( entry[TodoEntry.TITLE]!!.value.toString() )
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

                                    Button(onClick = {
                                        isUpdating = true
                                        titleField = entry[TodoEntry.TITLE]!!.value!!
                                        descriptionFiled = entry[TodoEntry.DESCRIPTION]!!.value!!
                                        statusFiled = entry[TodoEntry.STATUS]!!.value!!
                                        isCreationTabVisible = true
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

                                }else {
                                    var editField by remember {
                                        mutableStateOf( "${entry[TodoEntry.DESCRIPTION]!!.value}" )
                                    }
                                    TextField(value = editField , onValueChange ={
                                        editField = it
                                    } , modifier = Modifier.fillMaxWidth() )
                                    Spacer(modifier = Modifier.height( 5.dp ))
                                    Button(onClick = {
                                        updateEntry(
                                            entry[TodoEntry.TITLE]!!.value!!,
                                            editField,
                                            entry[TodoEntry.STATUS]!!.value!!.taskStatus
                                        )
                                        editMode = false
                                    }) {
                                        Text(text = "Apply" )
                                    }
                                    BackHandler ( editMode ) {
                                        editMode = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // creation button
        AnimatedVisibility(
            visible = isCreationTabVisible ,
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
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                    ) {
                        TextField(value = titleField, onValueChange = { titleField = it } ,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true ,
                            label = {
                                Text(text = "Title" )
                            }
                        )
                    }
                }

                item {
                    TextField(value = descriptionFiled, onValueChange = { descriptionFiled = it }, label = {
                        Text(text = "Description")
                    } , modifier = Modifier
                        .padding(vertical = 10.dp)
                        .fillParentMaxWidth()
                    )
                }

                item {
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
                                statusFiled = entry.toString()
                            }) {
                                Icon(
                                    entry.icon
                                    , "" ,
                                    tint = if ( statusFiled == entry.identifier ) Color.DarkGray else LocalContentColor.current ,
                                    modifier = Modifier
                                        .padding( horizontal = 2.dp )
                                )
                            }
                            Spacer(modifier = Modifier.width( 5.dp ))

                        }
                    }
                }

                item {
                    var buttonTitle by remember {
                        mutableStateOf( if ( isUpdating ) "Update" else "Add Entry" )
                    }
                   Row ( modifier = Modifier.fillMaxWidth() ,
                       horizontalArrangement = Arrangement.SpaceEvenly
                   ) {
                       Button(onClick = {
                           CoroutineScope( Dispatchers.Main ).launch {
                               if ( titleField.isBlank() ) {
                                   buttonTitle = "Title cannot be empty"
                                   delay( 1000 )
                                   buttonTitle = if ( isUpdating ) "Update" else "Add Entry"
                               } else {
                                   if (!isUpdating ) add( titleField , descriptionFiled , statusFiled.taskStatus )
                                   else updateEntry( titleField , descriptionFiled , statusFiled.taskStatus )
                               }
                           }
                       } , modifier = Modifier
                           .weight(1f)
                           .padding(horizontal = 5.dp) ) {
                           Text(text = buttonTitle  )
                       }

                       Button(onClick = {
                           isCreationTabVisible = false
                           titleField = ""
                           descriptionFiled = ""
                           statusFiled = TaskStatus.TODO.toString()
                           isUpdating = false
                       } , modifier = Modifier
                           .weight(1f)
                           .padding(horizontal = 5.dp)) {
                           Text(text = "Close"  )
                       }
                   }
                }
            }
            BackHandler( isCreationTabVisible ) {
                isCreationTabVisible = false
                titleField = ""
                descriptionFiled = ""
                statusFiled = TaskStatus.TODO.toString()
                isUpdating = false
            }
        }
    }

}