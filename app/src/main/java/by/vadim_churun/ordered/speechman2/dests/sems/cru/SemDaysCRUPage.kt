package by.vadim_churun.ordered.speechman2.dests.sems.cru

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.adapters.SemDaysAdapter
import by.vadim_churun.ordered.speechman2.model.objects.SeminarBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import kotlinx.android.synthetic.main.semdays_cru_page.view.*
import kotlinx.android.synthetic.main.semdays_cru_page.view.fabAdd


/** Provides read and update of general information about a [Seminar]. **/
class SemDaysCRUPage: SeminarCRUPage
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        /** This {@link View} receives CRU-related actions with componentID set to this value. **/
        val CRU_COMPONENT_ID = 2
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var lastBuilder: SeminarBuilder? = null
    private var lastEditable: Boolean? = null

    override val componentID: Int
        get() = SemDaysCRUPage.CRU_COMPONENT_ID

    override val layoutResID: Int
        get() = R.layout.semdays_cru_page

    override fun onApplyBuilder(builder: SeminarBuilder)
    {
        lastBuilder = builder
        recvDays.layoutManager = recvDays.layoutManager
            ?: LinearLayoutManager(super.getContext())
        val newAdapter = SemDaysAdapter(super.getContext(), builder, super.viewModel.actionSubject)
        recvDays.swapAdapter(newAdapter, true)

        lastEditable?.also {
            newAdapter.isEditable = it
        }
    }

    override fun onEditableChanged(isEditable: Boolean)
    {
        fabAdd.isVisible = isEditable

        val adapter = recvDays.adapter as? SemDaysAdapter
        adapter?.isEditable = isEditable

        lastEditable = isEditable
    }

    override fun onWriteChanges(dest: SeminarBuilder)
    { /* SeminarBuilder is kept up-to-date. */ }


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

        fabAdd.setOnClickListener {
            val semBuilder = lastBuilder ?: return@setOnClickListener
            val newDayBuilder: SeminarBuilder.DayBuilder
            if(semBuilder.dayBuilders.isEmpty())
                newDayBuilder = SeminarBuilder.DayBuilder()
            else
                newDayBuilder = SeminarBuilder.DayBuilder.suggest(semBuilder.dayBuilders.last())
            semBuilder.dayBuilders.add(newDayBuilder)
            super.viewModel.actionSubject
                .onNext( SpeechManAction.PublishSeminarBuilder(semBuilder) )
        }
    }
}