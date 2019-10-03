package by.vadim_churun.ordered.speechman2.model.objects

import by.vadim_churun.ordered.speechman2.model.warning.DataWarning
import by.vadim_churun.ordered.speechman2.remote.lack.DataLack


class RemoteData(
    val requestID: Int,
    val entities: List<Any>,
    val lacks: List< DataLack<*,*> >,
    val warnings: List< DataWarning<*> > )
{
    class Builder(
        var requestID: Int,
        val entities: MutableList<Any> = mutableListOf(),
        val lacks: MutableList<DataLack<*,*>> = mutableListOf(),
        val warnings: MutableList<DataWarning<*>> = mutableListOf() )
    {
        fun build()
            = RemoteData(requestID, entities, lacks, warnings)
    }

    fun toBuilder()
        = Builder(requestID,
            entities.toMutableList(),
            lacks.toMutableList(),
            warnings.toMutableList() )
}