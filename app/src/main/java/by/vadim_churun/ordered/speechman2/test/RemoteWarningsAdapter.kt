package by.vadim_churun.ordered.speechman2.test

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.model.warning.*
import kotlinx.android.synthetic.main.test_listitem.view.*


class RemoteWarningsAdapter
(val context: Context, val warnings: List< DataWarning<*> >):
RecyclerView.Adapter<RemoteWarningsAdapter.WarningViewHolder>()
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class WarningViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val tvTitle = itemView.tvTitle
        val tvInfo = itemView.tvInfo
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // OBJECT BINDING METHODS:

    private fun bindPersonNameExistsWarning
    (holder: WarningViewHolder, warning: PersonNameExistsWarning)
    {
        holder.tvTitle.text = "PersonNameExistsWarning"
        holder.tvInfo.text = "ID: ${warning.ID}\n" +
                "Name: ${warning.name}\n" +
                "Type ID: ${warning.personTypeID}"
    }

    private fun bindSeminarNameAndCityExistWarning
    (holder: WarningViewHolder, warning: SeminarNameAndCityExistWarning)
    {
        holder.tvTitle.text = "SeminarNameAndCityExistWarning${if(warning.isLogicallyDeleted) " /* DEL */" else ""}"
        holder.tvInfo.text = "ID: ${warning.ID}\n" +
                "Name: ${warning.name}\n" +
                "Location: ${warning.city}; ${warning.address}\n" +
                "Costing: ${warning.costing.name}"
    }

    private fun bindProductNameExistsWarning
    (holder: WarningViewHolder, warning: ProductNameExistsWarning)
    {
        holder.tvTitle.text = "ProductNameExistsWarning${if(warning.isLogicallyDeleted) " /* DEL */" else ""}"
        holder.tvInfo.text = "ID: ${warning.ID}\n" +
                "Name: ${warning.name}\n" +
                "Cost: ${warning.cost}\n" +
                "Count: B=${warning.countBoxes}, C=${warning.countCase}"
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION:

    override fun getItemCount(): Int
        = warnings.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarningViewHolder
        = LayoutInflater.from(context)
            .inflate(R.layout.test_listitem, parent, false)
            .let { WarningViewHolder(it) }

    override fun onBindViewHolder(holder: WarningViewHolder, position: Int)
    {
        val warning = warnings[position]
        when(warning)
        {
            is PersonNameExistsWarning -> bindPersonNameExistsWarning(holder, warning)
            is SeminarNameAndCityExistWarning -> bindSeminarNameAndCityExistWarning(holder, warning)
            is ProductNameExistsWarning -> bindProductNameExistsWarning(holder, warning)
            else -> throw TypeCastException("Unknown warning type ${warning.javaClass.name}")
        }
    }
}