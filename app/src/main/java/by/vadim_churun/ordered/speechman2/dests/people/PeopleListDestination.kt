package by.vadim_churun.ordered.speechman2.dests.people

import android.os.*
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.vadim_churun.ordered.speechman2.*
import by.vadim_churun.ordered.speechman2.adapters.PeopleAdapter
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.dialogs.people.AddPersonDialog
import by.vadim_churun.ordered.speechman2.model.filters.PeopleFilter
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.people_list_destination.*
import kotlinx.android.synthetic.main.people_list_destination.etCount


/** Displays the full list of people (optionally - a filtered list). **/
class PeopleListDestination:
    SpeechManFragment(R.layout.people_list_destination)
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private fun setAdapter(people: List<Person>)
    {
        etCount.text = super.getString(R.string.fs_people_count, people.size)

        if(recvPeople.layoutManager == null)
            recvPeople.layoutManager = LinearLayoutManager(super.requireContext())
        val newAdapter = PeopleAdapter(
            super.requireContext(), people, findNavController(), super.requireFragmentManager() )
        recvPeople.swapAdapter(newAdapter, true)
    }

    private fun setTypesMenu(types: List<PersonType>)
    {
        spFilterType.adapter = ArrayAdapter(
            super.requireContext(), android.R.layout.simple_spinner_dropdown_item, types )
        chbFilterType.isEnabled = types.isNotEmpty()
    }


    private fun setupTypesFiltering()
    {
        // Uncheckable unless existing people types are known.
        chbFilterType.isChecked = false; chbFilterType.isEnabled = false

        fun updateFilter()
        {
            val newFilter = PeopleFilter(vSearch.query.toString(),
                if(chbFilterType.isChecked) (spFilterType.selectedItem as PersonType).ID else null )
            super.viewModel.actionSubject.onNext(
                SpeechManAction.SetPeopleFilter(newFilter) )
        }

        spFilterType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {   }
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long)
                = updateFilter()
        }

        vSearch.setOnQueryTextListener( object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean
            {
                updateFilter()
                return true   // Handled the query.
            }

            override fun onQueryTextSubmit(query: String?): Boolean
                = true   // The query has been handled.
        } )
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()

    private fun observePeople()
        = super.viewModel.createPeopleObservable()
            .doOnNext { people ->
                setAdapter(people)
                super.viewModel.actionSubject.onNext(
                    SpeechManAction.RequestPersonInfos(people) )
            }.subscribe()

    private fun observeTypes()
    {
        // TODO: Subscribe to people types list observable.
    }

    private fun observePersonInfos()
        = super.viewModel.createPersonInfoObservable()
            .doOnNext { info ->
                if( Looper.myLooper() != Looper.getMainLooper() )
                    throw Exception("Applying PersonInfo not on the UI thread")
                val adapter = recvPeople.adapter as? PeopleAdapter
                adapter?.setPersonInfo(info)
            }.subscribe()


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // NAVIGATION

    private fun showAddPersonDialog()
        = AddPersonDialog().show(super.requireFragmentManager(), null)


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        setupTypesFiltering()
        fabAddPerson.setOnClickListener { showAddPersonDialog() }
        //super.setupSearchViewLayoutBehaviour(vSearch, tvDestTitle)
    }

    override fun onStart()
    {
        super.onStart()
        disposable.add(observePeople())
        // disposable.add(observeTypes())
        disposable.add(observePersonInfos())
    }

    override fun onStop()
    {
        super.onStop()
        disposable.clear()
    }
}