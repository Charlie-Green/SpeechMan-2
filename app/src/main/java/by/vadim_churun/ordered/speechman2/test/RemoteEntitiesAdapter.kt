package by.vadim_churun.ordered.speechman2.test

import android.content.Context
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.entities.*
import kotlinx.android.synthetic.main.test_listitem.view.*


class RemoteEntitiesAdapter(
    val context: Context,
    val objs: List<Any>
): RecyclerView.Adapter<RemoteEntitiesAdapter.ObjectViewHolder>()
{
    class ObjectViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val tvTitle = itemView.tvTitle
        val tvInfo = itemView.tvInfo
    }


    private fun onBindPerson(holder: ObjectViewHolder, person: Person)
    {
        holder.tvTitle.text = "Person"
        holder.tvInfo.text = "ID: ${person.ID}\n" +
                "Name: ${person.name}\n" +
                "Type: ${person.personTypeID}"
    }

    private fun onBindSeminar(holder: ObjectViewHolder, seminar: Seminar)
    {
        holder.tvTitle.text = "Seminar${if(seminar.isLogicallyDeleted) " /*DEL*/" else ""}"
        holder.tvInfo.text = "ID: ${seminar.ID}\n" +
                "Name: ${seminar.name}\n" +
                "Location: ${seminar.city}; ${seminar.address}\n" +
                "Costing: ${seminar.costing.name}\n"
        "Content: ${if(seminar.content.length < 12) seminar.content else seminar.content.substring(0, 8) + "..."}"
    }

    private fun onBindProduct(holder: ObjectViewHolder, product: Product)
    {
        holder.tvTitle.text = "Product${if(product.isLogicallyDeleted) " /* DEL */" else ""}"
        holder.tvInfo.text = "ID: ${product.ID}\n" +
                "Name: ${product.name}\n" +
                "Cost: ${product.cost}\n" +
                "Count: B=${product.countBoxes}, C=${product.countCase}"
    }

    private fun onBindAppointment(holder: ObjectViewHolder, appoint: Appointment)
    {
        holder.tvTitle.text = "Appointment(P=${appoint.personID}; S=${appoint.seminarID})"
        holder.tvInfo.text = "Paid: ${appoint.purchase} out of ${appoint.cost}\n" +
                "History: ${appoint.historyStatus.name}\n" +
                "Deleted: ${appoint.isLogicallyDeleted}"
    }

    private fun onBindOrder(holder: ObjectViewHolder, order: Order)
    {
        holder.tvTitle.text = "Order(Pson=${order.personID}; Pduct=${order.productID})"
        holder.tvInfo.text = "Paid: ${order.purchase}\n" +
                "History: ${order.historyStatus.name}\n" +
                "Deleted: ${order.isLogicallyDeleted}"
    }

    private fun onBindSemDay(holder: ObjectViewHolder, day: SemDay)
    {
        holder.tvTitle.text = "SemDay(sem=${day.seminarID})"
        holder.tvInfo.text = "${day.start.timeInMillis}, lasts for ${day.duration} min"
    }

    private fun onBindSemCost(holder: ObjectViewHolder, cost: SemCost)
    {
        holder.tvTitle.text = "SemCost(sem=${cost.seminarID})"
        holder.tvInfo.text = "Participants: ${cost.minParticipants}\n" +
                "Date: ${cost.minDate.timeInMillis}\n" +
                "Money: ${cost.cost}"
    }


    override fun getItemCount(): Int
            = objs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewHolder
            = LayoutInflater.from(context)
        .inflate(R.layout.test_listitem, parent, false)
        .let { ObjectViewHolder(it) }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int)
    {
        val entity = objs[position]
        when(entity)
        {
            is Person -> onBindPerson(holder, entity)
            is Seminar -> onBindSeminar(holder, entity)
            is Product -> onBindProduct(holder, entity)
            is Appointment -> onBindAppointment(holder, entity)
            is Order -> onBindOrder(holder, entity)
            is SemDay -> onBindSemDay(holder, entity)
            is SemCost -> onBindSemCost(holder, entity)
            else -> throw IllegalArgumentException("Unkown entity ${entity.javaClass.name}")
        }
    }
}