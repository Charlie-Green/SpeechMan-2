package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.entities.Seminar
import by.vadim_churun.ordered.speechman2.db.objs.Money
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

    class LackViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val imgvEntityType = itemView.imgvEntityType
        val tvTitle = itemView.tvTitle
        val tvInfo = itemView.tvInfo
        val etMissingData = itemView.etMissingData
        val imgvDiscard = itemView.imgvDiscard
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // DATA:

    private val fills = MutableList<String?>(lacks.size) { null }


    private var infos: List<DataLackInfo?>? = null

    fun setLackInfos(lackInfos: List<DataLackInfo?>)
    {
        infos = lackInfos
        super.notifyDataSetChanged()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // HELP METHODS:

    private fun listenMissingText(holder: LackViewHolder, listener: (CharSequence) -> Unit)
    {
        holder.etMissingData.addTextChangedListener( object: TextWatcher {
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

    private fun bindAppointPurchase
    (holder: LackViewHolder, lack: AppointmentPurchaseLack, info: DataLackInfo.AppointmentInfo?)
    {
        holder.imgvEntityType.visibility = View.INVISIBLE
        val promptText = context.getString(
            R.string.fs_fill_appoint_purchase_lack, info?.personName ?: "", info?.seminarName ?: "" )
        holder.tvInfo.text = promptText
        holder.tvTitle.setText(R.string.mi_appoint_purchase)
        holder.etMissingData.isEnabled = false
        holder.etMissingData.setOnClickListener {
            info ?: return@setOnClickListener
            MoneyPickerDialog(promptText, null) { money ->
                lack.fill(money)
            }.show(fragmMan, null)
        }
    }

    private fun bindAppointCost
    (holder: LackViewHolder, lack: AppointmentCostLack, info: DataLackInfo.AppointmentInfo?)
    {
        holder.imgvEntityType.visibility = View.INVISIBLE
        holder.tvTitle.setText(R.string.mi_appoint_cost)
        val promptText = context.getString(
            R.string.fs_fill_appoint_cost_lack, info?.seminarName ?: "", info?.personName ?: "")
        holder.tvInfo.text = promptText
        holder.etMissingData.isEnabled = false
        holder.etMissingData.setOnClickListener {
            info ?: return@setOnClickListener
            MoneyPickerDialog(promptText, null) { money ->
                lack.fill(money)
            }
        }
    }

    private fun bindAppointMoney
    (holder: LackViewHolder, lack: AppointmentMoneyLack, info: DataLackInfo.AppointmentInfo?)
    {
        holder.imgvEntityType.visibility = View.INVISIBLE
        holder.tvTitle.setText(R.string.mi_appoint_money)
        val promptText = context.getString(
            R.string.fs_fill_appoint_money_lack, info?.seminarName ?: "", info?.personName ?: "")
        holder.tvInfo.text = promptText
        holder.etMissingData.isEnabled = false
        holder.etMissingData.setOnClickListener {
            info ?: return@setOnClickListener
            PurchaseCostPickerDialog(promptText, null, null) { purchase, cost ->
                lack.fill( AppointmentMoneyLack.MissedData(purchase, cost) )
            }
        }
    }

    private fun bindOrderPurchase
    (holder: LackViewHolder, lack: OrderPurchaseLack, info: DataLackInfo.OrderInfo?)
    {
        holder.imgvEntityType.visibility = View.INVISIBLE
        holder.tvTitle.setText(R.string.mi_order_purchase)
        val promptText = context.getString(
            R.string.fs_fill_order_purchase_lack, info?.personName ?: "", info?.productName ?: "" )
        holder.tvInfo.text = promptText
        holder.etMissingData.isEnabled = false
        holder.etMissingData.setOnClickListener {
            info ?: return@setOnClickListener
            MoneyPickerDialog(promptText, null) { money ->
                lack.fill(money)
            }
        }
    }

    private fun bindProductCost(holder: LackViewHolder,  position: Int)
    {
        val lack = lacks[position] as ProductCostLack
        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_product)
        holder.tvTitle.text = lack.name
        holder.tvInfo.text = context.getString(
            R.string.fs_fill_product_cost_lack, lack.countCase, lack.countBoxes )
        holder.etMissingData.isEnabled = true
        holder.etMissingData.setText("")
        listenMissingText(holder) { text ->
            try {
                fills[position] = Money.parse(text.toString()).toString()
            } catch(exc: Exception) {
                fills[position] = null
            }
        }
    }

    private fun bindSeminarName(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position] as SeminarNameLack
        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_seminar)
        holder.tvTitle.text = ""
        val address = if(lack.address.isEmpty())
            context.getString(R.string.la_seminar_address_empty)
            else lack.address
        val content = if(lack.content.isEmpty())
            context.getString(R.string.la_seminar_content_empty)
            else lack.content
        holder.tvInfo.text = context.getString(
            R.string.fs_fill_seminar_name_lack, lack.city, address, content )
        holder.etMissingData.isEnabled = true
        holder.etMissingData.setText("")
        listenMissingText(holder) { text ->
            val name = text.toString()
            if(lack.validate(name))
                fills[position] = name
            else
                fills[position] = null
        }
    }

    private fun bindSeminarCity(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position] as SeminarCityLack
        holder.imgvEntityType.visibility = View.VISIBLE
        holder.tvTitle.text = lack.name
        val address = if(lack.address.isEmpty())
            context.getString(R.string.la_seminar_address_empty)
            else lack.address
        val content = if(lack.content.isEmpty())
            context.getString(R.string.la_seminar_content_empty)
            else lack.content
        holder.tvInfo.text = context.getString(R.string.fs_fill_seminar_city_lack, address, content)
        holder.etMissingData.isEnabled = true
        holder.etMissingData.setText("")
        listenMissingText(holder) { text ->
            val city = text.toString()
            if(lack.validate(city))
                fills[position] = city
            else
                fills[position] = null
        }
    }

    private fun bindSemCostMoney(holder: LackViewHolder, position: Int)
    {
        holder.imgvEntityType.visibility = View.VISIBLE
        holder.imgvEntityType.setImageResource(R.drawable.ic_seminar)
        holder.tvTitle.text = "SeminarName"

        val info = infos?.get(position) as DataLackInfo.SemCostInfo?
        val infoText: String
        when(info?.costing)
        {
            Seminar.CostingStrategy.FIXED -> {
                infoText = context.getString(R.string.fs_fill_semcost_lack_f)
            }
            Seminar.CostingStrategy.PARTICIPANTS -> {
                infoText = context.getString(
                    R.string.fs_fill_semcost_lack_p, info.seminarName, info.minParticipants )
            }
            Seminar.CostingStrategy.DATE -> {
                infoText = context.getString(
                    R.string.fs_fill_semcost_lack_d,
                    info.seminarName,
                    DATE_FORMAT.format(info.minDate.time)
                )
            }
            Seminar.CostingStrategy.PARTICIPANTS_DATE -> {
                infoText = context.getString(
                    R.string.fs_fill_semcost_lack_pd,
                    info.minParticipants,
                    DATE_FORMAT.format(info.minDate.time)
                )
            }
            else /* null */ -> {
                infoText = ""
            }
        }
        holder.tvInfo.text = infoText

        holder.etMissingData.isEnabled = true
        listenMissingText(holder) { text ->
            try {
                fills[position] = Money.parse(text.toString()).toString()
            } catch(exc: Exception) {
                fills[position] = null
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION:

    override fun getItemCount(): Int
        = lacks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(context)
            .inflate(R.layout.data_lack_listitem, parent, true)
            .let { LackViewHolder(it) }

    override fun onBindViewHolder(holder: LackViewHolder, position: Int)
    {
        val lack = lacks[position]
        when(lack)
        {
            is AppointmentPurchaseLack -> {
                val info = infos?.get(position) as DataLackInfo.AppointmentInfo
                bindAppointPurchase(holder, lack, info)
            }

            is AppointmentCostLack -> {
                val info = infos?.get(position) as DataLackInfo.AppointmentInfo?
                bindAppointCost(holder, lack, info)
            }

            is AppointmentMoneyLack -> {
                val info = infos?.get(position) as DataLackInfo.AppointmentInfo?
                bindAppointMoney(holder, lack, info)
            }

            is OrderPurchaseLack -> {
                val info = infos?.get(position) as DataLackInfo.OrderInfo?
                bindOrderPurchase(holder, lack, info)
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
            }

            else -> {
                val superTypeName = DataLack::class.java.simpleName
                val subtypeName = lack.javaClass.simpleName
                throw IllegalArgumentException("Unknown $superTypeName subtype $subtypeName")
            }
        }
    }
}