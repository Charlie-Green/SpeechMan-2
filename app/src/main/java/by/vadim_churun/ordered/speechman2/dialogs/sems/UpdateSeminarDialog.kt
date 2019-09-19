package by.vadim_churun.ordered.speechman2.dialogs.sems

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import by.vadim_churun.ordered.speechman2.*
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.model.objects.SeminarBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.update_seminar_dialog.*


/** Allows the user to save their changes to a {@link Seminar} or rollback them. **/
class UpdateSeminarDialog: SpeechManFragment(R.layout.update_seminar_dialog)
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private val disposable = CompositeDisposable()
    private var semBuilder: SeminarBuilder? = null
    private var isSaveAllowed = false

    private fun subscribeBuilder()
        = super.viewModel.createSeminarBuilderObservable()
            .doOnNext { builder ->
                semBuilder = builder
                prbSeminarLoad.isVisible = !isSaveAllowed
            }.subscribe()

    private fun subscribeBuilderUptodate()
        = super.viewModel.sbuilderUptodateSubject
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { isUpToDate ->
                isSaveAllowed = isUpToDate
                (prbSeminarLoad.isVisible) = (!isUpToDate) || (semBuilder == null)
            }.subscribe()

    private fun updateSeminar()
    {
        if(!isSaveAllowed) return
        val mBuilder = semBuilder ?: return
        prbSeminarLoad.visibility = View.VISIBLE
        super.viewModel.actionSubject
            .onNext( SpeechManAction.SaveSeminar(mBuilder) )
        super.dismiss()
    }

    private fun rollbackSeminar()
    {
        prbSeminarLoad.visibility = View.VISIBLE

        var sem: Seminar? = null
        var days: List<SemDay>? = null
        var costs: List<SemCost>? = null

        fun publishBuilder()
        {
            SeminarBuilder.from(sem!!, days!!, costs!!).let {
                SpeechManAction.PublishSeminarBuilder(it)
            }.also {
                super.viewModel.actionSubject.onNext(it)
            }

            super.dismiss()
        }

        val mBuilder = semBuilder ?: return
        super.viewModel.createSeminarObservable(mBuilder.ID!!)
            .doOnNext { seminar ->
                sem = seminar
                if(days != null && costs != null)
                    publishBuilder()
            }.subscribe()
            .also { disposable.add(it) }
        super.viewModel.createSemDaysObservable(mBuilder.ID!!)
            .doOnNext { semdays ->
                days = semdays
                if(sem != null && costs != null)
                    publishBuilder()
            }.subscribe()
            .also { disposable.add(it) }
        super.viewModel.createSemCostsObservable(mBuilder.ID!!)
            .doOnNext { semcosts ->
                costs = semcosts
                if(sem != null && days != null)
                    publishBuilder()
            }.subscribe()
            .also { disposable.add(it) }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        buCancel.setOnClickListener { rollbackSeminar() }
        buUpdate.setOnClickListener { updateSeminar() }
    }

    override fun onStart()
    {
        super.onStart()
        disposable.add(subscribeBuilder())
        disposable.add(subscribeBuilderUptodate())
    }

    override fun onStop()
    {
        disposable.clear()
        super.onStop()
    }
}