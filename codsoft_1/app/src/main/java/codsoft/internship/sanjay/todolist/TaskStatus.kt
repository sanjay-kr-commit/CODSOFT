package codsoft.internship.sanjay.todolist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

enum class TaskStatus(
    val icon : ImageVector,
    val identifier : String
) {
    COMPLETED( Icons.Filled.Done , "COMPLETED" ) ,
    TODO( Icons.Filled.Clear , "TODO" ) ,
    UNKNOWN( Icons.Filled.Warning , "UNKNOWN" );

    override fun toString(): String {
        return identifier
    }

    companion object {
        val String.taskStatus : TaskStatus
            get() {
                for ( status in TaskStatus.entries ) {
                    if ( status.toString() == this ) {
                        return status
                    }
                }
                return UNKNOWN
            }
    }
}