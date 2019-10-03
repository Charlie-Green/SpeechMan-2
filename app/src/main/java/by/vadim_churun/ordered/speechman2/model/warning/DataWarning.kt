package by.vadim_churun.ordered.speechman2.model.warning


/** These instances are generated to warn the user that some data retrieved from the remote service
  * is put into a question and chances are it shouldn't be added to the local database. **/
abstract class DataWarning<ObjectType>
{
    protected abstract fun createObject(): ObjectType


    enum class ConfirmStatus {
        CONFIRMED,
        DENIED,
        NOT_DEFINED
    }
    var confirmStatus = ConfirmStatus.NOT_DEFINED


    fun produceObject(): ObjectType
    {
        if(confirmStatus != ConfirmStatus.CONFIRMED)
            throw IllegalStateException("Adding data is not confirmed")
        return createObject()
    }
}