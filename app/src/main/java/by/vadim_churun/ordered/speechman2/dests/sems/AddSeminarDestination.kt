package by.vadim_churun.ordered.speechman2.dests.sems

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.SpeechManFragment
import by.vadim_churun.ordered.speechman2.adapters.CRUSeminarPagerAdapter
import by.vadim_churun.ordered.speechman2.model.objects.SeminarBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.add_seminar_destination.*


class AddSeminarDestination: SpeechManFragment(R.layout.add_seminar_destination)
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private val disposable = CompositeDisposable()
    private var pageCruId = -1

    private fun subscribeAction()
        = super.viewModel.actionSubject
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { action ->
                when(action)
                {
                    is SpeechManAction.NavigateError -> {
                        val adapter = vPager.adapter as? CRUSeminarPagerAdapter
                        adapter?.getErrorSourcePosition(action.errorMessageResID)
                            ?.also { vPager.setCurrentItem(it, true) }
                        buSave.isEnabled = true
                    }
                }
            }.subscribe()

    private fun onPageSelected(position: Int)
    {
        super.viewModel.actionSubject
            .onNext( SpeechManAction.CommitCRUObject(pageCruId) )
        val pagerAdapter = vPager.adapter as CRUSeminarPagerAdapter
        pageCruId = pagerAdapter.getComponentIdForPage(position)
        tvStep.text = super.requireContext()
            .getString(R.string.fs_step, position + 1, pagerAdapter.getCount())
        tvPurpose.text = pagerAdapter.getPagePurpose(position)
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        val pagerAdapter = CRUSeminarPagerAdapter(super.requireContext())

        vPager.adapter = pagerAdapter
        vPager.addOnPageChangeListener( object: ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int)
                = this@AddSeminarDestination.onPageSelected(position)
        } )
        onPageSelected(0)

        buSave.setOnClickListener {
            super.viewModel.actionSubject
                .onNext( SpeechManAction.SaveCRUObject(pageCruId) )
            buSave.isEnabled = false    // Prevent double click.
        }

        if(savedInstanceState == null) {
            // Building a Seminar from scratch.
            super.viewModel.actionSubject
                .onNext( SpeechManAction.PublishSeminarBuilder(SeminarBuilder()) )
        }
    }

    override fun onStart()
    {
        super.onStart()
        disposable.add(subscribeAction())
    }

    override fun onStop()
    {
        disposable.clear()
        super.viewModel.actionSubject
            .onNext( SpeechManAction.CommitCRUObject(pageCruId) )
        super.onStop()
    }
}