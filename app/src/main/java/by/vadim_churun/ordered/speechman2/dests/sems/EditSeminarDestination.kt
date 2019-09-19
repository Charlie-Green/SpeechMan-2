package by.vadim_churun.ordered.speechman2.dests.sems

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.adapters.CRUSeminarPagerAdapter
import by.vadim_churun.ordered.speechman2.dests.sems.cru.SeminarGeneralCRUPage
import by.vadim_churun.ordered.speechman2.dialogs.sems.UpdateSeminarDialog
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import kotlinx.android.synthetic.main.edit_seminar_destination.*


/** Allows to lookup and edit information about a {@link Seminar}.
  * A {@link SeminarBuilder} corresponding to the {@link Seminar} being looked up/edited
  * must have been submitted for the Destination to work properly. **/
class EditSeminarDestination: SpeechManFragment(R.layout.edit_seminar_destination)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        const val KEY_IS_INITIALLY_EDITABLE = "canEditInit"
        const val KEY_START_CRU_ID = "initCruId"
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var pageCruId = -1
    private var pagePosition = -1


    private fun setAdapter()
    {
        prbPagerLoad.visibility = View.GONE
        val pagerAdapter = CRUSeminarPagerAdapter(super.requireContext())

        vPager.adapter = pagerAdapter
        var pageRestored = false
        vPager.addOnPageChangeListener( object: ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int)
            {
                val actionSubject = this@EditSeminarDestination.viewModel.actionSubject
                actionSubject.onNext( SpeechManAction.CommitCRUObject(pageCruId) )
                pageCruId = pagerAdapter.getComponentIdForPage(position)
                setEditable(chbEditable.isChecked, !pageRestored, false)
                tvPurpose.text = pagerAdapter.getPagePurpose(position)

                pageRestored = true
                pagePosition = position
            }
        } )
    }

    private fun setEditable
    (isEditable: Boolean, keepAction: Boolean, showPromptDialog: Boolean)
    {
        SpeechManAction.ChangeCRUEditable(pageCruId, chbEditable.isChecked).also {
            super.viewModel.actionSubject.onNext(it)
            if(keepAction)
                super.viewModel.keepAction(it)
        }

        buSave.isVisible = isEditable

        if(!isEditable && showPromptDialog) {
            super.viewModel.sbuilderUptodateSubject.onNext(false)
            super.viewModel.actionSubject
                .onNext( SpeechManAction.CommitCRUObject(pageCruId) )
            UpdateSeminarDialog().apply {
                isCancelable = false
                show(super.requireFragmentManager(), null)
            }
        }
    }

    private fun requestSave()
    {
        super.viewModel.actionSubject
            .onNext( SpeechManAction.SaveCRUObject(pageCruId) )
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        setAdapter()
        buSave.setOnClickListener { requestSave() }

        val DEFAULT_CRU_ID = SeminarGeneralCRUPage.CRU_COMPONENT_ID
        val isEditable: Boolean
        if(savedInstanceState == null) {
            // Act as was requested by the caller.
            isEditable = super.getArguments()?.getBoolean(KEY_IS_INITIALLY_EDITABLE, true) ?: true
            pageCruId = super.getArguments()?.getInt(KEY_START_CRU_ID, DEFAULT_CRU_ID) ?: DEFAULT_CRU_ID
        } else {
            // Use the saved state.
            isEditable = savedInstanceState.getBoolean(KEY_IS_INITIALLY_EDITABLE, true)
            pageCruId = savedInstanceState.getInt(KEY_START_CRU_ID, DEFAULT_CRU_ID)
        }

        chbEditable.isChecked = isEditable
        val adapter = (vPager.adapter as CRUSeminarPagerAdapter)
        pagePosition = adapter.getPageForComponentId(pageCruId)
        vPager.currentItem = pagePosition
        tvPurpose.text = adapter.getPagePurpose(pagePosition)
        pageCruId = adapter.getComponentIdForPage(pagePosition)
        setEditable(isEditable, keepAction = true, showPromptDialog = false)

        chbEditable.setOnCheckedChangeListener { buttonView, isChecked ->
            setEditable(isChecked, keepAction = false, showPromptDialog = true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_INITIALLY_EDITABLE, chbEditable.isChecked)
        outState.putInt(KEY_START_CRU_ID, pagePosition)
    }
}