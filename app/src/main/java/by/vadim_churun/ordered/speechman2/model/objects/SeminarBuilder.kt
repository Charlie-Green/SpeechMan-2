package by.vadim_churun.ordered.speechman2.model.objects

import android.net.Uri
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.db.objs.Money
import java.util.Calendar


class SeminarBuilder(
    var ID: Int? = null,
    var name: String = "",
    var city: String = "",
    var address: String = "",
    var content: String = "",
    var imageUri: Uri? = null,
    var costing: Seminar.CostingStrategy = Seminar.CostingStrategy.FIXED )
{
    class DayBuilder(
        var start: Calendar = Calendar.getInstance(),
        var duration: Short = 480 )
    {
        companion object
        {
            fun from(day: SemDay)
                = DayBuilder(day.start.clone() as Calendar, day.duration)

            fun suggest(basedOn: DayBuilder): DayBuilder
            {
                val startClone = basedOn.start.clone() as Calendar
                return DayBuilder(
                    startClone.apply { add(Calendar.DAY_OF_MONTH, 1) },
                    basedOn.duration
                )
            }
        }

        fun build(semID: Int)
            = SemDay(null, semID, start, duration)
    }

    class CostBuilder(
        var minParticipants: Int = 0,
        var minDate: Calendar = Calendar.getInstance(),
        // TODO: Localize the default value.
        var cost: Money = Money(100.00f, "BYN") )
    {
        companion object
        {
            fun from(cost: SemCost)
                = CostBuilder(cost.minParticipants,
                    cost.minDate.clone() as Calendar,
                    Money(cost.cost.amount, cost.cost.currency) )

            fun suggest(basedOn: CostBuilder, strategy: Seminar.CostingStrategy): CostBuilder
            {
                var particips = basedOn.minParticipants
                var date = basedOn.minDate.clone() as Calendar
                var cost = basedOn.cost.clone() as Money
                if(Seminar.doesCostDependOnParticipants(strategy))
                    particips *= 2
                if(Seminar.doesCostDependOnDate(strategy))
                    date.add(Calendar.DAY_OF_MONTH, 7)
                return CostBuilder(particips, date, cost)
            }
        }

        fun build(semID: Int)
            = SemCost(null, semID, minParticipants, minDate, cost)
    }


    fun buildSeminar()
        = Seminar(ID, name, city, address, content, imageUri, costing, false)

    val dayBuilders = ArrayList<SeminarBuilder.DayBuilder>()
    fun buildDays()
        = dayBuilders.map { dayBuilder -> dayBuilder.build(ID!!) }

    val costBuilders = ArrayList<SeminarBuilder.CostBuilder>()
    fun buildCosts()
        = costBuilders.map { costBuilder -> costBuilder.build(ID!!) }

    companion object
    {
        fun from(source: Seminar,
            days: Collection<SemDay>? = null, costs: Collection<SemCost>? = null ): SeminarBuilder
        {
            val semBuilder = SeminarBuilder(
                source.ID,
                source.name,
                source.city,
                source.address,
                source.content,
                source.imageUri,
                source.costing
            )

            days?.also {
                for(day in it)
                {
                    semBuilder.dayBuilders.add( DayBuilder.from(day) )
                }
            }

            costs?.also {
                for(cost in it)
                {
                    semBuilder.costBuilders.add( CostBuilder.from(cost) )
                }
            }

            return semBuilder
        }
    }
}