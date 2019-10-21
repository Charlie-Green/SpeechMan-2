package by.vadim_churun.ordered.speechman2.model.objects

import by.vadim_churun.ordered.speechman2.model.warning.DataWarning
import by.vadim_churun.ordered.speechman2.remote.lack.DataLack


class RemoteData(
    val requestID: Int,
    val entities: List<Any>,
    val lacks: List< DataLack<*,*> >,
    val warnings: List< DataWarning<*> >,
    val entityActions: List< RemoteData.EntityAction > )
{
    enum class EntityAction {
        INSERT,
        UPDATE,
        DELETE
    }

    class Builder(
        var requestID: Int,
        var entities: MutableList<Any> = mutableListOf(),
        var lacks: MutableList<DataLack<*,*>> = mutableListOf(),
        var warnings: MutableList<DataWarning<*>> = mutableListOf(),
        var entityActions: MutableList<RemoteData.EntityAction> = mutableListOf() )
    {
        fun build()
            = RemoteData(requestID, entities, lacks, warnings, entityActions)
    }

    fun toBuilder()
        = Builder(requestID,
            entities.toMutableList(),
            lacks.toMutableList(),
            warnings.toMutableList(),
            entityActions.toMutableList() )
}