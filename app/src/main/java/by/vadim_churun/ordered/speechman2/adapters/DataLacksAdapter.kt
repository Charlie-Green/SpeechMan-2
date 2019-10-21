package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.text.*
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.entities.Seminar
import by.vadim_churun.ordered.speechman2.model.lack_info.DataLackInfo
import by.vadim_churun.ordered.speechman2.remote.lack.*
import by.vadim_churun.ordered.speechman2.views.*
import kotlinx.android.synthetic.main.data_lack_listitem.view.*
import java.text.SimpleDateFormat


class DataLacksAdapter(
    val context: Context,
    val fragmMan: FragmentManager,
    val lacks: List<DataLack<*,*>>
): RecyclerView.Adapter<DataLacksAdapter.LackViewHolder>()
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        // TODO: Localize.
        private val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy")
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class LackViewHolder(itemView: View):
    RecyclerView.ViewHolder(itemView)
    {
        val vlltBack = itemView.vlltBack
        val vlltFront = itemView.vlltFront
        val imgvEntityType = itemView.imgvEntityType
        val tvTitle = itemView.tvTitle
        val tvDeletedWarning = itemView.tvDeletedWarning
        val tvInfo = itemView.tvInfo
        val imgvDiscard = itemView.imgvDiscard
        val buRestore = itemView.buRestore
        val pholderMissingData = itemView.pholderMissingData
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // DATA:

    private var infos: List<DataLackInfo?>? = null

    fun setLackInfos(lackInfos: List<DataLackInfo?>)
    {
        infos = lackInfos
        super.notifyDataSetChanged()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // HELP METHODS:

    private fun ensureMissingDataFieldIsEditable
    (holder: LackViewHolder, editable: Boolean, position: Int): TextView
    {
        var params: FrameLayout.LayoutParams? = null
        if(holder.pholderMissingData.childCount != 0) {
            val oldView = holder.pholderMissingData.getChildAt(0) as TextView
            if((oldView is EditText) == editable)
                return oldView
            params = oldView.layoutParams as FrameLayout.LayoutParams
            holder.pholderMissingData.removeAllViews()
        }

        val newView: TextView = if(editable) SingleWatcherEditText(context) else TextView(context)
        params = params ?: FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        holder.pholderMissingData.addView(newView, params)
        return newView
    }

    private fun listenMissingText(holder: LackViewHolder, position: Int, listener: (CharSequence) -> Unit)
    {
        val et = holder.pholderMissingData.getChildAt(0) as EditText
        et.addTextChangedListener( object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int)
            {    }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {    }

            override fun afterTextChanged(s: Editable)
            { listener(s) }
        } )
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // HOLDER BINDING:

    private fun bindAppointPurchase(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position] as AppointmentPurchaseLack
        val info = infos?.get(position) as DataLackInfo.AppointmentInfo?

        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_seminar)
        holder.tvTitle.setText(R.string.mi_appoint_purchase)
        holder.tvDeletedWarning.isVisible = lack.isDeleted
        val promptText = context.getString(
            R.string.fs_fill_appoint_purchase_lack, info?.personName ?: "", info?.seminarName ?: "" )
        holder.tvInfo.text = promptText

        val tvMissingData = ensureMissingDataFieldIsEditable(holder, false, position)
        tvMissingData.text = lack.filledData?.toString()
            ?: context.getString(R.string.msg_data_not_entered_click)
        tvMissingData.setOnClickListener {
            info ?: return@setOnClickListener
            MoneyPickerDialog(promptText, null) { money ->
                lack.fill(money)
                super.notifyItemChanged(position)
            }.show(fragmMan, null)
        }
    }

    private fun bindAppointCost(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position] as AppointmentCostLack
        val info = infos?.get(position) as DataLackInfo.AppointmentInfo?

        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_seminar)
        holder.tvTitle.setText(R.string.mi_appoint_cost)
        holder.tvDeletedWarning.isVisible = lack.isDeleted
        val promptText = context.getString(
            R.string.fs_fill_appoint_cost_lack,
            info?.personName ?: "",
            info?.seminarName ?: "",
            lack.purchase.toString()
        )
        holder.tvInfo.text = promptText

        val tvMissingData = ensureMissingDataFieldIsEditable(holder, false, position)
        tvMissingData.text = lack.filledData?.toString()
            ?: context.getString(R.string.msg_data_not_entered_click)
        tvMissingData.setOnClickListener {
            info ?: return@setOnClickListener
            MoneyPickerDialog(promptText, null) { money ->
                lack.fill(money)
                super.notifyItemChanged(position)
            }.show(fragmMan, null)
        }
    }

    private fun bindAppointMoney(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position] as AppointmentMoneyLack
        val info = infos?.get(position) as DataLackInfo.AppointmentInfo?

        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_seminar)
        holder.tvTitle.setText(R.string.mi_appoint_money)
        holder.tvDeletedWarning.isVisible = lack.isDeleted
        val promptText = context.getString(
            R.string.fs_fill_appoint_money_lack, info?.seminarName ?: "", info?.personName ?: "")
        holder.tvInfo.text = promptText

        val tvMissingData = ensureMissingDataFieldIsEditable(holder, false, position)
        tvMissingData.text = lack.filledData?.let {
            context.getString(R.string.fs_appoint_money, it.purchase, it.cost)
        } ?: context.getString(R.string.msg_data_not_entered_click)
        tvMissingData.setOnClickListener {
            info ?: return@setOnClickListener
            PurchaseCostPickerDialog(promptText, null, null) { purchase, cost ->
                lack.fill( AppointmentMoneyLack.MissingData(purchase, cost) )
                super.notifyItemChanged(position)
            }.show(fragmMan, null)
        }
    }

    private fun bindOrderPurchase(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position] as OrderPurchaseLack
        val info = infos?.get(position) as DataLackInfo.OrderInfo?

        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_product)
        holder.tvTitle.setText(R.string.mi_order_purchase)
        holder.tvDeletedWarning.isVisible = lack.isDeleted
        val promptText = context.getString(
            R.string.fs_fill_order_purchase_lack, info?.personName ?: "", info?.productName ?: "" )
        holder.tvInfo.text = promptText

        val tvMissingData = ensureMissingDataFieldIsEditable(holder, false, position)
        tvMissingData.text = lack.filledData?.toString()
            ?: context.getString(R.string.msg_data_not_entered_click)
        tvMissingData.setOnClickListener {
            Log.v(DataLacksAdapter::class.java.simpleName, "OrderPurchaseLack TextView clicked.")
            info ?: return@setOnClickListener
            MoneyPickerDialog(promptText, null) { money ->
                lack.fill(money)
                super.notifyItemChanged(position)
            }.show(fragmMan, null)
        }
    }

    private fun bindProductCost(holder: LackViewHolder,  position: Int)
    {
        val lack = lacks[position] as ProductCostLack
        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_product)
        holder.tvTitle.text = lack.name
        holder.tvDeletedWarning.isVisible = lack.isDeleted
        holder.tvInfo.text = context.getString(
            R.string.fs_fill_product_cost_lack, lack.countCase, lack.countBoxes )

        val tvMissingData = ensureMissingDataFieldIsEditable(holder, false, position)
        tvMissingData.text = lack.filledData?.toString()
            ?: context.getString(R.string.msg_data_not_entered_click)
        tvMissingData.setOnClickListener {
            Log.v(DataLacksAdapter::class.java.simpleName, "ProductCostLack TextView clicked.")
            val purpose = context.getString(
                R.string.fs_product_cost_lack_purpose, lack.name )
            MoneyPickerDialog(purpose, null) { money ->
                lack.fill(money)
                super.notifyItemChanged(position)
            }.show(fragmMan, null)
        }
    }

    private fun bindSeminarName(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position] as SeminarNameLack
        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_seminar)
        holder.tvTitle.setText(R.string.la_seminar_name_lack)
        holder.tvDeletedWarning.isVisible = lack.isDeleted
        val address = if(lack.address.isEmpty())
            context.getString(R.string.la_seminar_address_empty)
            else lack.address
        val content = if(lack.content.isEmpty())
            context.getString(R.string.la_seminar_content_empty)
            else lack.content
        holder.tvInfo.text = context.getString(
            R.string.fs_fill_seminar_name_lack, lack.city, address, content )

        val etMissingData = ensureMissingDataFieldIsEditable(holder, true, position) as EditText
        etMissingData.setHint(R.string.msg_data_not_entered)
        etMissingData.setText( lack.filledData ?: "" )
        listenMissingText(holder, position) { text ->
            Log.v(DataLacksAdapter::class.java.simpleName, "SeminarNameLack text: $text.")
            val name = text.toString()
            if(lack.validate(name))
                lack.fill(name)
            else
                lack.erase()
        }
    }

    private fun bindSeminarCity(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position] as SeminarCityLack
        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_seminar)
        holder.tvTitle.text = lack.name
        holder.tvDeletedWarning.isVisible = lack.isDeleted

        val address = if(lack.address.isEmpty())
            context.getString(R.string.la_seminar_address_empty)
            else lack.address
        val content = if(lack.content.isEmpty())
            context.getString(R.string.la_seminar_content_empty)
            else lack.content
        holder.tvInfo.text = context.getString(R.string.fs_fill_seminar_city_lack, address, content)

        val etMissingData = ensureMissingDataFieldIsEditable(holder, true, position) as EditText
        etMissingData.setHint(R.string.msg_data_not_entered)
        etMissingData.setText( lack.filledData ?: "" )
        listenMissingText(holder, position) { text ->
            Log.v(DataLacksAdapter::class.java.simpleName, "SeminarCityLack text: $text.")
            val city = text.toString()
            if(lack.validate(city))
                lack.fill(city)
            else
                lack.erase()
        }
    }

    private fun bindSemCostMoney(holder: LackViewHolder, position: Int)
    {
        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_seminar)

        val lack = lacks[position] as SemCostMoneyLack
        val info = infos?.get(position) as DataLackInfo.SemCostInfo?
        holder.tvTitle.text = info?.seminarName ?: ""
        holder.tvDeletedWarning.isVisible = false

        val infoText: String
        when(info?.costing)
        {
            Seminar.CostingStrategy.FIXED -> {
                infoText = context.getString(R.string.fs_fill_semcost_lack_f)
            }
            Seminar.CostingStrategy.PARTICIPANTS -> {
                infoText = context.getString(
                    R.string.fs_fill_semcost_lack_p, info.seminarName, lack.minParticipants )
            }
            Seminar.CostingStrategy.DATE -> {
                infoText = context.getString(
                    R.string.fs_fill_semcost_lack_d,
                    info.seminarName,
                    DATE_FORMAT.format(lack.minDate.time)
                )
            }
            Seminar.CostingStrategy.PARTICIPANTS_DATE -> {
                infoText = context.getString(
                    R.string.fs_fill_semcost_lack_pd,
                    lack.minParticipants,
                    DATE_FORMAT.format(lack.minDate.time)
                )
            }
            Seminar.CostingStrategy.DATE_PARTICIPANTS -> {
                infoText = context.getString(
                    R.string.fs_fill_semcost_lack_dp,
                    info.seminarName,
                    DATE_FORMAT.format(lack.minDate.time),
                    lack.minParticipants
                )
            }
            else /* null */ -> {
                infoText = ""
            }
        }
        holder.tvInfo.text = infoText

        val tvMissingData = ensureMissingDataFieldIsEditable(holder, false, position)
        tvMissingData.text = lack.filledData?.toString()
            ?: context.getString(R.string.msg_data_not_entered_click)
        tvMissingData.setOnClickListener {
            info ?: return@setOnClickListener
            MoneyPickerDialog(infoText, null) { money ->
                lack.fill(money)
                super.notifyItemChanged(position)
            }.show(fragmMan, null)
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION:

    override fun getItemCount(): Int
        = lacks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(context)
            .inflate(R.layout.data_lack_listitem, parent, false)
            .let { LackViewHolder(it) }

    override fun onBindViewHolder(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position]
        holder.pholderMissingData.getChildAt(0)?.isEnabled = !lack.isDiscarded
        if(lack.isDiscarded) {
            holder.vlltBack.alpha = 0.25f
            holder.vlltFront.visibility = View.VISIBLE
            holder.buRestore.setOnClickListener {
                lack.restore()
                super.notifyItemChanged(position)
            }
        } else {
            holder.vlltBack.alpha = 1.0f
            holder.vlltFront.visibility = View.INVISIBLE
            holder.imgvDiscard.setOnClickListener {
                lack.discard()
                super.notifyItemChanged(position)
            }
        }

        when(lack)
        {
            is AppointmentPurchaseLack -> {
                bindAppointPurchase(holder, position)
                holder.tvInfo.text = "Per=${lack.personID}, Sem=${lack.seminarID}"
            }

            is AppointmentCostLack -> {
                bindAppointCost(holder, position)
                holder.tvInfo.text = "Per=${lack.personID}, Sem=${lack.seminarID}"
            }

            is AppointmentMoneyLack -> {
                bindAppointMoney(holder, position)
                holder.tvInfo.text = "Per=${lack.personID}, Sem=${lack.seminarID}"
            }

            is OrderPurchaseLack -> {
                val info = infos?.get(position) as DataLackInfo.OrderInfo?
                bindOrderPurchase(holder, position)
                holder.tvInfo.text = "Per=${lack.personID}, Prd=${lack.productID}"
            }

            is ProductCostLack -> {
                bindProductCost(holder, position)
            }

            is SeminarNameLack -> {
                bindSeminarName(holder, position)
            }

            is SeminarCityLack -> {
                bindSeminarCity(holder, position)
            }

            is SemCostMoneyLack -> {
                bindSemCostMoney(holder, position)
                holder.tvInfo.text = "Sem=${lack.seminarID}"
            }

            else -> {
                val superTypeName = DataLack::class.java.simpleName
                val subtypeName = lack.javaClass.simpleName
                throw IllegalArgumentException("Unknown $superTypeName subtype $subtypeName")
            }
        }
    }
}