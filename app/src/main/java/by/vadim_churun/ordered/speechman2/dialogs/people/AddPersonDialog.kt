package by.vadim_churun.ordered.speechman2.dialogs.people

import android.content.*
import android.content.ClipboardManager
import android.os.Bundle
import android.text.*
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.add_person_dialog.*


class AddPersonDialog: SpeechManFragment(R.layout.add_person_dialog)
{
    private val LOGTAG = AddPersonDialog::class.java.simpleName


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private val disposable = CompositeDisposable()


    private fun setTypes(types: List<PersonType>)
    {
        chbAssignType.isEnabled = types.isNotEmpty()
        spAssignType.adapter = ArrayAdapter(
            super.requireContext(), android.R.layout.simple_spinner_dropdown_item, types )
    }

    private fun pasteName()
    {
        val clipMan = ContextCompat.getSystemService(
            super.requireContext(), ClipboardManager::class.java )
        if( clipMan!!.hasPrimaryClip()
            && clipMan.primaryClip!!.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) )
            etName.setText(clipMan.primaryClip!!.getItemAt(0).text)
        else
            Log.i(LOGTAG, "Nothing to paste.")
    }

    private fun applyPerson()
    {
        val typeID = if(chbAssignType.isChecked)
            (spAssignType.selectedItem as PersonType).ID
            else null
        Person(null, etName.text.toString(), typeID).let {
            Log.i(LOGTAG, "Adding person ${it.name} of typeID ${it.personTypeID}")
            SpeechManAction.AddPerson(it)
        }.also {
            super.viewModel.actionSubject.onNext(it)
        }
    }


    private fun trackNameValidity()
    {
        buAdd.isEnabled = super.viewModel.validatePersonName(etName.text)
        etName.addTextChangedListener( object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {   }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {   }
            override fun afterTextChanged(s: Editable)
            { buAdd.isEnabled = this@AddPersonDialog.viewModel.validatePersonName(s) }
        } )
    }

    private fun observeTypes()
    {
        // TODO
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        chbAssignType.isEnabled = false
        //disposable.add(observeTypes())
        trackNameValidity()
        buCancel.setOnClickListener { super.dismiss() }
        buAdd.setOnClickListener { applyPerson(); super.dismiss() }
        imgvPaste.setOnClickListener { pasteName() }
    }

    override fun onDismiss(dialog: DialogInterface)
    {
        Log.i(LOGTAG, "Dialog dissmissed")
        super.onDismiss(dialog)
        disposable.clear()
    }
}