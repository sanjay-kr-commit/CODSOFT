package codsoft.internship.sanjay.todolist

enum class TodoEntry(
    private val identifier : String
) {
    TABLE( "TODO_LIST" ) ,
    ID( "TIMESTAMP" ) ,
    TITLE( "TITLE" ) ,
    DESCRIPTION( "DESCRIPTION" ) ,
    STATUS( "STATUS" ),
    UNIDENTIFIED( "UNIDENTIFIED" );

    override fun toString(): String {
        return identifier
    }

    companion object {
        val String.transform : TodoEntry
            get() {
                for ( entry in entries ) {
                    if ( entry.identifier == this ) return entry
                }
                return UNIDENTIFIED
            }
    }

}