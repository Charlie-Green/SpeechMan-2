package by.vadim_churun.ordered.speechman2.model.warning


/** These instances are generated to warn the user that some data retrieved from the remote service
  * is put into a question and chances are it shouldn't be added to the local database. **/
abstract class DataWarning<ObjectType>
{
    protected abstract fun createObject(): ObjectType


    private var confirmed: Boolean? = null

    var isConfirmed: Boolean
        get() = confirmed
            ?: throw NullPointerException("Data hasn't been neither confirmed nor denied")
        set(decision) { confirmed = decision }


    fun produceObject(): ObjectType
    {
        if(confirmed != true)
            throw IllegalStateException("Adding data hasn't been confirmed")
        return createObject()
    }
}