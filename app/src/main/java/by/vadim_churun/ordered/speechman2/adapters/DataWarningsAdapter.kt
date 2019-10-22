package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.model.warning.*
import kotlinx.android.synthetic.main.data_warnings_listitem.view.*


class DataWarningsAdapter(
    val context: Context,
    val warnings: List<DataWarning<*>>
): RecyclerView.Adapter<DataWarningsAdapter.WarningViewHolder>()
{
    private val LOGTAG = DataWarningsAdapter::class.java.simpleName

    //////////////////////////////////////////////////////////////////////////////////////////////
    // ACTIONS:

    class ActionSpinnerItem(
        private val res: Resources,
        val labelResId: Int )
    {
        override fun toString()
            = res.getString(labelResId)
    }

    private val ACTION_SPINNER_ITEMS = listOf(
        ActionSpinnerItem(context.resources, R.string.mi_update_warning),
        ActionSpinnerItem(context.resources, R.string.mi_drop_warning),
        ActionSpinnerItem(context.resources, R.string.mi_duplicate_warning)
    )

    private fun setActionSelection(holder: WarningViewHolder, warning: DataWarning<*>)
    {
        if(warning.action == DataWarning.Action.NOT_DEFINED)
            warning.action = DataWarning.Action.UPDATE
        holder.spAction.setSelection(
            when(warning.action)
            {
                DataWarning.Action.UPDATE    -> 0
                DataWarning.Action.DROP      -> 1
                DataWarning.Action.DUPLICATE -> 2
                else -> throw IllegalStateException("Unknown DataWarning.Action")
            }
        )
    }

    private fun listenActionChange(holder: WarningViewHolder, warning: DataWarning<*>)
    {
        holder.spAction.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?)
            {   }

            override fun onItemSelected
            (parent: AdapterView<*>, view: View, position: Int, id: Long)
            {
                val selectedAction = holder.spAction.selectedItem as ActionSpinnerItem

                warning.action = when(selectedAction.labelResId) {
                    R.string.mi_update_warning    -> DataWarning.Action.UPDATE
                    R.string.mi_drop_warning      -> DataWarning.Action.DROP
                    R.string.mi_duplicate_warning -> DataWarning.Action.DUPLICATE
                    else -> throw Exception(
                        "Unknown action label \"${context.getString(selectedAction.labelResId)}\"" )
                }
            }
        }
    }

    fun setActionToAll(action: DataWarning.Action)
    {
        for(warning in warnings)
        {
            warning.action = action
        }
        super.notifyDataSetChanged()
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    // STYLING:

    private var colorHeaderText: Int
    private var colorItemText: Int

    init {
        val typval = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorPrimary, typval, true)
        colorHeaderText = typval.data
        context.theme.resolveAttribute(android.R.attr.textColor, typval, true)
        colorItemText = typval.data
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class WarningViewHolder(itemView: View, val actions: List<ActionSpinnerItem>):
    RecyclerView.ViewHolder(itemView)
    {
        val imgvEntityType = itemView.imgvEntityType
        val tvTitle = itemView.tvTitle
        val tvInfo = itemView.tvInfo
        val spAction = itemView.spAction.apply {
            adapter = ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, actions)
        }
        val tvActionHeader = itemView.tvActionHeader
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BIND METHODS:

    private fun bindHeader(holder: WarningViewHolder)
    {
        holder.tvTitle.text = ""
        holder.tvInfo.setText(R.string.la_data_warning_info)
        holder.tvActionHeader.setText(R.string.la_data_warning_action)
    }

    private fun bindPersonName(holder: WarningViewHolder, position: Int)
    {
        val warning = warnings[position] as PersonNameExistsWarning

        holder.imgvEntityType.setImageResource(R.drawable.ic_person)
        holder.tvTitle.text = warning.name
        holder.tvInfo.setText(R.string.msg_person_name_exists)
    }

    private fun bindSeminarNameAndCity(holder: WarningViewHolder, position: Int)
    {
        val warning = warnings[position] as SeminarNameAndCityExistWarning

        holder.imgvEntityType.setImageResource(R.drawable.ic_seminar)
        holder.tvTitle.text = warning.name
        holder.tvInfo.text = context.getString(
            R.string.fs_seminar_name_and_city_exist, warning.city )
    }

    private fun bindProductName(holder: WarningViewHolder, position: Int)
    {
        val warning = warnings[position] as ProductNameExistsWarning

        holder.imgvEntityType.setImageResource(R.drawable.ic_product)
        holder.tvTitle.text = warning.name
        holder.tvInfo.setText(R.string.msg_product_name_exists)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION:

    override fun getItemCount(): Int
        = warnings.size + 1    // +1 for the header.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarningViewHolder
        = LayoutInflater.from(context)
            .inflate(R.layout.data_warnings_listitem, parent, false)
            .let { WarningViewHolder(it, ACTION_SPINNER_ITEMS) }

    override fun onBindViewHolder(holder: WarningViewHolder, position: Int)
    {
        if(position == 0) {
            holder.imgvEntityType.visibility = View.INVISIBLE
            holder.tvTitle.visibility = View.GONE
            holder.tvInfo.alpha = 1f; holder.tvInfo.setTextColor(colorHeaderText)
            holder.tvActionHeader.visibility = View.VISIBLE
            holder.spAction.visibility = View.GONE
            bindHeader(holder)
            return
        }

        val warning = warnings[position - 1]
        setActionSelection(holder, warning)
        listenActionChange(holder, warning)

        holder.imgvEntityType.visibility = View.VISIBLE
        holder.tvTitle.visibility = View.VISIBLE
        holder.tvInfo.alpha = 0.4f; holder.tvInfo.setTextColor(colorItemText)
        holder.tvActionHeader.visibility = View.GONE
        holder.spAction.visibility = View.VISIBLE
        when(warning)
        {
            is PersonNameExistsWarning        -> bindPersonName(holder, position - 1)
            is SeminarNameAndCityExistWarning -> bindSeminarNameAndCity(holder, position - 1)
            is ProductNameExistsWarning       -> bindProductName(holder, position - 1)
        }
    }
}