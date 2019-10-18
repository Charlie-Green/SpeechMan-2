package by.vadim_churun.ordered.speechman2.model.warning


/** These instances are generated to warn the user that some data retrieved from the remote service
  * is put into a question and chances are it shouldn't be added to the local database. **/
abstract class DataWarning<ObjectType>
{
    protected abstract fun createObject(): ObjectType


    enum class Action {
        UPDATE,
        DROP,
        DUPLICATE,
        NOT_DEFINED
    }
    var action = Action.NOT_DEFINED


    fun produceObject(): ObjectType
    {
        if(action != Action.UPDATE && action != Action.DUPLICATE)
            throw IllegalStateException("Adding data is not confirmed")
        return createObject()
    }
}