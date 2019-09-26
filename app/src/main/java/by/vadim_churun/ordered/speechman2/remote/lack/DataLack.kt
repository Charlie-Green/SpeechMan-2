package by.vadim_churun.ordered.speechman2.remote.lack


/** Represents a fact of missing some piece of data.
  * Instances will be generated when a file retrieved from the remote server misses mandatory data. **/
abstract class DataLack<MissedDataType, ObjectType>
{
    protected abstract fun buildObject(missedData: MissedDataType): ObjectType
    abstract fun validate(potentialData: MissedDataType): Boolean


    private var datapiece: MissedDataType? = null

    fun fill(data: MissedDataType)
    {
        if(!validate(data))
            throw IllegalArgumentException("DataLack filled with invalid data")
        datapiece = data
    }

    fun buildObject(): ObjectType
        = datapiece?.let { buildObject(it) }
        ?: throw IllegalStateException("This DataLack hasn't been filled")
}