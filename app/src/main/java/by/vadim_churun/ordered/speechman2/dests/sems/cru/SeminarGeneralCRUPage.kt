package by.vadim_churun.ordered.speechman2.dests.sems.cru

import android.content.Context
import android.util.AttributeSet
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.model.objects.SeminarBuilder
import kotlinx.android.synthetic.main.seminar_general_cru_page.view.*


/** Provides read and update of general information about a [Seminar]. **/
class SeminarGeneralCRUPage: SeminarCRUPage
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        /** This {@link View} receives CRU-related actions with componentID set to this value. **/
        val CRU_COMPONENT_ID = 0
    }


    constructor(context: Context):
        super(context)
    constructor(context: Context, attrs: AttributeSet?):
        super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
        super(context, attrs, defStyleAttr)


    override val componentID: Int
        get() = SeminarGeneralCRUPage.CRU_COMPONENT_ID

    override val layoutResID: Int
        get() = R.layout.seminar_general_cru_page

    override fun onApplyBuilder(builder: SeminarBuilder)
    {
        etName.setText(builder.name)
        etCity.setText(builder.city)
        etAddress.setText(builder.address)
        etContent.setText(builder.content)
    }

    override fun onEditableChanged(isEditable: Boolean)
    {
        etName.isEnabled    = isEditable
        etCity.isEnabled    = isEditable
        etAddress.isEnabled = isEditable
        etContent.isEnabled = isEditable
    }

    override fun onWriteChanges(dest: SeminarBuilder)
    {
        dest.name    = etName.text.toString()
        dest.city    = etCity.text.toString()
        dest.address = etAddress.text.toString()
        dest.content = etContent.text.toString()
    }
}