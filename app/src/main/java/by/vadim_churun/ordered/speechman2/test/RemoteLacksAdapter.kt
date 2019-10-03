package by.vadim_churun.ordered.speechman2.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.remote.lack.*
import kotlinx.android.synthetic.main.test_listitem.view.*


class RemoteLacksAdapter(
    val context: Context,
    val lacks: List<DataLack<*,*>>
): RecyclerView.Adapter<RemoteLacksAdapter.LackViewHolder>()
{
    class LackViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val tvTitle = itemView.tvTitle
        val tvInfo = itemView.tvInfo
    }


    private fun onBindAppointmentCostLack(holder: LackViewHolder, lack: AppointmentCostLack)
    {
        holder.tvTitle.text = "AppointmentCostLack${if(lack.isDeleted) "/* DEL */" else ""}"
        holder.tvInfo.text = "P=${lack.personID}; S=${lack.seminarID}\n" +
                "Paid: ${lack.purchase}\n" +
                "History: ${lack.historyStatus.name}"
    }

    private fun onBindAppointmentMoneyLack(holder: LackViewHolder, lack: AppointmentMoneyLack)
    {
        holder.tvTitle.text = "AppointmentMoneyLack${if(lack.isDeleted) "/* DEL */" else ""}"
        holder.tvInfo.text = "P=${lack.personID}; S=${lack.seminarID}\n" +
                "History: ${lack.historyStatus.name}"
    }

    private fun onBindAppointmentPurchaseLack(holder: LackViewHolder, lack: AppointmentPurchaseLack)
    {
        holder.tvTitle.text = "AppointmentPurchaseLack${if(lack.isDeleted) " /* DEL */" else ""}"
        holder.tvInfo.text = "P=${lack.personID}; S=${lack.seminarID}\n" +
                "Cost: ${lack.cost}\n" +
                "History: ${lack.historyStatus.name}"
    }

    private fun onBindOrderPurchaseLack(holder: LackViewHolder, lack: OrderPurchaseLack)
    {
        holder.tvTitle.text = "OrderPurchaseLack${if(lack.isDeleted) "/* DEL */" else ""}"
        holder.tvInfo.text = "Pson=${lack.personID}; Pduct=${lack.productID}\n" +
                "History: ${lack.historyStatus.name}"
    }

    private fun onBindProductCostLack(holder: LackViewHolder, lack: ProductCostLack)
    {
        holder.tvTitle.text = "ProductCostLack"
        holder.tvInfo.text = "ID: ${lack.ID}\n" +
                "Name: ${lack.name}\n"
        "Count: B=${lack.countBoxes}; C=${lack.countCase}"
    }

    private fun onBindSemCostMoneyLack(holder: LackViewHolder, lack: SemCostMoneyLack)
    {
        holder.tvTitle.text = "SemCostMoneyLack"
        holder.tvInfo.text = "Seminar ID: ${lack.seminarID}\n" +
                "Participants: ${lack.minParticipants}\n" +
                "Date: ${lack.minDate.timeInMillis}"
    }

    private fun onBindSeminarCityLack(holder: LackViewHolder, lack: SeminarCityLack)
    {
        holder.tvTitle.text = "SeminarCityLack"
        holder.tvInfo.text = "Seminar ID: ${lack.ID}\n" +
                "Name: ${lack.name}\n" +
                "Address: ${lack.address}\n" +
                "Costing: ${lack.costing.name}\n" +
                "Content: ${if(lack.content.length < 12) lack.content else lack.content.substring(0, 8) + "..."}\n" +
                "Deleted: ${lack.isDeleted}"
    }

    private fun onBindSeminarNameLack(holder: LackViewHolder, lack: SeminarNameLack)
    {
        holder.tvTitle.text = "SeminarNameLack"
        holder.tvInfo.text = "Seminar ID: ${lack.ID}\n" +
                "City: ${lack.city}\n" +
                "Address: ${lack.address}\n" +
                "Costing: ${lack.costing.name}\n" +
                "Content: ${if(lack.content.length < 12) lack.content else lack.content.substring(0, 8) + "..."}\n" +
                "Deleted: ${lack.isDeleted}"
    }


    override fun getItemCount(): Int
            = lacks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LackViewHolder
            = LayoutInflater.from(context)
        .inflate(R.layout.test_listitem, parent, false)
        .let { LackViewHolder(it) }

    override fun onBindViewHolder(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position]
        when(lack)
        {
            is AppointmentCostLack -> onBindAppointmentCostLack(holder, lack)
            is AppointmentMoneyLack -> onBindAppointmentMoneyLack(holder, lack)
            is AppointmentPurchaseLack -> onBindAppointmentPurchaseLack(holder, lack)
            is OrderPurchaseLack -> onBindOrderPurchaseLack(holder, lack)
            is ProductCostLack -> onBindProductCostLack(holder, lack)
            is SemCostMoneyLack -> onBindSemCostMoneyLack(holder, lack)
            is SeminarCityLack -> onBindSeminarCityLack(holder, lack)
            is SeminarNameLack -> onBindSeminarNameLack(holder, lack)
        }
    }
}