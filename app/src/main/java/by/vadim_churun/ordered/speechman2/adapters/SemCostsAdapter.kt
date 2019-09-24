package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.entities.Seminar
import by.vadim_churun.ordered.speechman2.dialogs.sems.SemCostParticipantsDialog
import by.vadim_churun.ordered.speechman2.model.objects.SeminarBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import by.vadim_churun.ordered.speechman2.views.DialogFactory
import by.vadim_churun.ordered.speechman2.views.MoneyPickerDialog
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.semcosts_listitem.view.*
import java.text.SimpleDateFormat
import java.util.Calendar


class SemCostsAdapter(val context: Context,
    val semBuilder: SeminarBuilder,
    val actionSubject: PublishSubject<SpeechManAction>,
    val fragmMan: FragmentManager
): RecyclerView.Adapter<SemCostsAdapter.SemCostViewHolder>()
{
    //////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        // TODO: Localize.
        private val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy")
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class SemCostViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val tvDate      = itemView.tvDate
        val tvParticips = itemView.tvParticips
        val tvCost      = itemView.tvCost
        val imgvDelete  = itemView.imgvDelete
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // UPDATING DATA:

    private var editable = true
    var isEditable: Boolean
        get() = editable
        set(value) {
            editable = value
            super.notifyDataSetChanged()
        }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION:

    override fun getItemCount(): Int
        = semBuilder.costBuilders.size + 1     // +1 for the header.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemCostViewHolder
        = LayoutInflater.from(context)
            .inflate(R.layout.semcosts_listitem, parent, false)
            .let { SemCostViewHolder(it) }

    override fun onBindViewHolder(holder: SemCostViewHolder, position: Int)
    {
        holder.tvDate.isVisible = Seminar.doesCostDependOnDate(semBuilder.costing)
        holder.tvDate.isEnabled = (position != 0)
        holder.tvParticips.isVisible = Seminar.doesCostDependOnParticipants(semBuilder.costing)
        holder.tvParticips.isEnabled = (position != 0)
        holder.tvCost.isEnabled = (position != 0)

        if(position == 0) {
            holder.tvDate.setText(R.string.la_semcost_date)
            holder.tvParticips.setText(R.string.la_semcost_particips)
            holder.tvCost.setText(R.string.la_semcost_cost)
            holder.imgvDelete.visibility = View.INVISIBLE
            return
        }

        val costBuilder = semBuilder.costBuilders[position - 1]
        holder.tvDate.text = DATE_FORMAT.format(costBuilder.minDate.time)
        context.getString(R.string.fs_semcost_particips, costBuilder.minParticipants)
            .also { holder.tvParticips.text = it }
        holder.tvCost.text = "${costBuilder.cost}"

        if(Seminar.doesCostDependOnDate(semBuilder.costing)
            && editable ) {
            holder.tvDate.setOnClickListener {
                DialogFactory.setNextSuggestion(costBuilder.minDate)
                DialogFactory.pickDate(context) { day, month, year ->
                    costBuilder.minDate.apply {
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.MONTH, month)
                        set(Calendar.YEAR, year)
                    }
                    actionSubject.onNext( SpeechManAction.PublishSeminarBuilder(semBuilder) )
                }
            }
        } else {
            holder.tvDate.setOnClickListener(null)
        }

        if(Seminar.doesCostDependOnParticipants(semBuilder.costing)
            && editable ) {
            holder.tvParticips.setOnClickListener {
                val dialogArgs = Bundle()
                dialogArgs.putInt(SemCostParticipantsDialog.KEY_COST_POSITION, position - 1)
                SemCostParticipantsDialog().apply {
                    arguments = dialogArgs
                    show(fragmMan, null)
                }
            }
        } else {
            holder.tvDate.setOnClickListener(null)
        }

        if(isEditable) {
            holder.tvCost.setOnClickListener {
                var purpose = context.getString(R.string.la_semcost_cost_purpose)
                if(Seminar.doesCostDependOnDate(semBuilder.costing))
                    purpose += "\n- " + context.getString(
                        R.string.fs_semcost_date_atleast, DATE_FORMAT.format(costBuilder.minDate.time) )
                if(Seminar.doesCostDependOnParticipants(semBuilder.costing))
                    purpose += "\n- " + context.getString(
                        R.string.fs_semcost_particips_atleast, costBuilder.minParticipants )

                MoneyPickerDialog(purpose, costBuilder.cost) { money ->
                    costBuilder.cost = money
                    actionSubject.onNext( SpeechManAction.PublishSeminarBuilder(semBuilder) )
                }.show(fragmMan, null)
            }
        } else {
            holder.tvCost.setOnClickListener(null)
        }

        if(isEditable) {
            holder.imgvDelete.visibility = View.VISIBLE
            holder.imgvDelete.isEnabled = true
            holder.imgvDelete.setOnClickListener {
                holder.imgvDelete.isEnabled = false
                semBuilder.costBuilders.removeAt(position - 1)
                actionSubject.onNext( SpeechManAction.PublishSeminarBuilder(semBuilder) )
            }
        } else {
            holder.imgvDelete.visibility = View.INVISIBLE
        }
    }
}