package by.vadim_churun.ordered.speechman2.dests.sems.cru

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.adapters.SemCostsAdapter
import by.vadim_churun.ordered.speechman2.db.entities.Seminar
import by.vadim_churun.ordered.speechman2.model.objects.SeminarBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import by.vadim_churun.ordered.speechman2.views.MoneyPickerDialog
import kotlinx.android.synthetic.main.semcosts_cru_page.view.*
import kotlinx.android.synthetic.main.semcosts_cru_page.view.fabAdd
import java.util.*


/** Provides read and update of general information about a [Seminar]. **/
class SemCostsCRUPage: SeminarCRUPage
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        /** This {@link View} receives CRU-related actions with componentID set to this value. **/
        val CRU_COMPONENT_ID = 3
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // HELP:

    private val costings = arrayOf(
        Seminar.CostingStrategy.FIXED,
        Seminar.CostingStrategy.PARTICIPANTS,
        Seminar.CostingStrategy.DATE,
        Seminar.CostingStrategy.PARTICIPANTS_DATE,
        Seminar.CostingStrategy.DATE_PARTICIPANTS
    )

    private val costingStrings = costings.map { costing ->
        Seminar.costingToString(super.getContext(), costing)
    }

    private fun applyCosting(position: Int)
    {
        val builder = this@SemCostsCRUPage.lastBuilder ?: return
        builder.costing = costings[position]
        this@SemCostsCRUPage.viewModel.actionSubject
            .onNext( SpeechManAction.PublishSeminarBuilder(builder) )
    }

    private fun pickFixedCost()
    {
        val builder = lastBuilder ?: return
        val dialog = MoneyPickerDialog(
            super.getContext().getString(R.string.la_pick_fixed_semcost),
            null ) { money ->
                SeminarBuilder.CostBuilder(0, Calendar.getInstance(), money).also {
                    builder.costBuilders.clear()
                    builder.costBuilders.add(it)
                }
                super.viewModel.actionSubject
                    .onNext( SpeechManAction.PublishSeminarBuilder(builder) )
            }
        dialog.show( (super.getContext() as AppCompatActivity).supportFragmentManager, null )
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IMPLEMENTATIONS:

    private var lastBuilder: SeminarBuilder? = null
    private var lastEditable: Boolean? = null

    override val componentID: Int
        get() = SemCostsCRUPage.CRU_COMPONENT_ID

    override val layoutResID: Int
        get() = R.layout.semcosts_cru_page

    override fun onApplyBuilder(builder: SeminarBuilder)
    {
        lastBuilder = builder

        val isFixed = (builder.costing == Seminar.CostingStrategy.FIXED)
        recvCosts.isVisible = !isFixed
        fabAdd.isVisible = !isFixed
        tvFixedCost.isVisible = isFixed && builder.costBuilders.isNotEmpty()
        buFixedCost.isVisible = isFixed && builder.costBuilders.isEmpty()

        if(builder.costBuilders.isNotEmpty())
            tvFixedCost.text = "${builder.costBuilders[0].cost}"
        spCosting.setSelection( costings.indexOf(builder.costing) )

        if(!isFixed) {
            recvCosts.layoutManager = recvCosts.layoutManager
                ?: LinearLayoutManager(super.getContext())
            val newAdapter = SemCostsAdapter(super.getContext(),
                builder,
                super.viewModel.actionSubject,
                (super.getContext() as AppCompatActivity).supportFragmentManager
            )
            recvCosts.swapAdapter(newAdapter, true)
            lastEditable?.also {
                newAdapter.isEditable = it
            }
        }

        prbCostsLoad.visibility = View.GONE
    }

    override fun onEditableChanged(isEditable: Boolean)
    {
        fabAdd.isVisible = isEditable
        val adapter = recvCosts.adapter as? SemCostsAdapter
        adapter?.isEditable = isEditable
        lastEditable = isEditable

    }

    override fun onWriteChanges(dest: SeminarBuilder)
    {
        dest.costing = costings[spCosting.selectedItemPosition]
        /* SemCosts are handled by the adapter and the pickFixedCost method. */
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    constructor(context: Context):
        super(context)
    constructor(context: Context, attrs: AttributeSet?):
        super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
        super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow()
    {
        super.onAttachedToWindow()

        tvFixedCost.setOnClickListener { pickFixedCost() }
        buFixedCost.setOnClickListener { pickFixedCost() }

        fabAdd.setOnClickListener {
            val semBuilder = lastBuilder ?: return@setOnClickListener

            val newCostBuilder: SeminarBuilder.CostBuilder
            if(semBuilder.costBuilders.isEmpty())
                newCostBuilder = SeminarBuilder.CostBuilder()
            else
                newCostBuilder = SeminarBuilder.CostBuilder.suggest(
                    semBuilder.costBuilders.last(), semBuilder.costing )
            semBuilder.costBuilders.add(newCostBuilder)
            super.viewModel.actionSubject
                .onNext( SpeechManAction.PublishSeminarBuilder(semBuilder) )
        }

        spCosting.adapter = ArrayAdapter(super.getContext(),
            R.layout.support_simple_spinner_dropdown_item,
            costingStrings )
        spCosting.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            private var isInited = false

            override fun onNothingSelected(parent: AdapterView<*>) {   }
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long)
            {
                if(!isInited) {
                    isInited = true
                    return
                }
                applyCosting(position)
            }
        }
    }
}