package by.vadim_churun.ordered.speechman2.dests.sems

import android.graphics.Color
import android.os.Bundle
import android.util.*
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import by.vadim_churun.ordered.speechman2.*
import by.vadim_churun.ordered.speechman2.db.entities.*
import by.vadim_churun.ordered.speechman2.dests.sems.cru.*
import by.vadim_churun.ordered.speechman2.dialogs.sems.ParticipantsListDialog
import by.vadim_churun.ordered.speechman2.model.objects.*
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import com.google.android.material.appbar.AppBarLayout
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.seminar_detail_destination.*
import java.text.SimpleDateFormat


class SeminarDetailDestination: SpeechManFragment(R.layout.seminar_detail_destination)
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        // TODO: Localize
        private val DATETIME_FORMAT = SimpleDateFormat("dd.MM.yyyy, HH:mm")

        const val KEY_SEMINAR_ID = "semID"
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NAVIGATION:

    private fun commitBuilder(): Boolean
    {
        val mSeminar = seminar ?: return false
        val mDays = days ?: return false
        val mCosts = costs ?: return false

        prbSeminarLoad.visibility = View.VISIBLE
        val semBuilder = SeminarBuilder.from(mSeminar, mDays, mCosts)
        super.viewModel.actionSubject
            .onNext( SpeechManAction.PublishSeminarBuilder(semBuilder) )
        return true
    }

    private fun showParticipants()
    {
        val sem = seminar ?: return
        val dialogArgs = Bundle()
        dialogArgs.putInt(ParticipantsListDialog.KEY_SEMINAR_ID, sem.ID!!)
        ParticipantsListDialog().apply {
            arguments = dialogArgs
            show(super.requireFragmentManager(), null)
        }
    }

    private fun showTutors()
    {
        // TODO
    }

    private fun navigateLookup(startCruId: Int)
    {
        if(!commitBuilder()) return
        val destArgs = Bundle()
        destArgs.putBoolean(EditSeminarDestination.KEY_IS_INITIALLY_EDITABLE, false)
        destArgs.putInt(EditSeminarDestination.KEY_START_CRU_ID, startCruId)
        findNavController().navigate(R.id.actLookupSeminar, destArgs)
    }

    private fun navigateEdit()
    {
        if(commitBuilder())
            findNavController().navigate(R.id.actEditDetailedSeminar)
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UI:

    private var seminar: Seminar? = null
    private var days: List<SemDay>? = null
    private var costs: List<SemCost>? = null
    private var infoLoaded = false
    private var wantToolbarCollapsed = true

    private fun updateProgressBarByLoad()
    {
        prbSeminarLoad.isVisible =
            seminar == null || days == null || costs == null
    }

    private fun setupToolbar()
    {
        val typval = TypedValue()
        super.requireContext().theme.resolveAttribute(R.attr.colorPrimary, typval, true)
        val colorPrimary = typval.data
        super.requireContext().theme.resolveAttribute(android.R.attr.colorBackground, typval, true)
        val colorBack = typval.data

        appbarLayout.addOnOffsetChangedListener( object: AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(layout: AppBarLayout, offset: Int)
            {
                /** Returns a color component x, such that (x - value0) / (value1 - value0) = ratio. **/
                fun mixColorComponents
                (value0: Int, value1: Int, ratio: Float): Int
                    = value0 + ratio.times(value1 - value0).toInt()

                val collapseRatio = -offset.toFloat() / layout.totalScrollRange.toFloat()
                val alpha = collapseRatio.times(255f).toInt()
                tbLayout.setBackgroundColor(colorPrimary.and(0x00ffffff).or(alpha shl 24))
                imgvAvatar.alpha = 1f - collapseRatio
                val transColor = Color.rgb(
                    mixColorComponents(
                        colorBack.and(0x00ff0000).shr(16), colorPrimary.and(0x00ff0000).shr(16), 1f - collapseRatio ),
                    mixColorComponents(
                        colorBack.and(0x0000ff00).shr(8), colorPrimary.and(0x00ff00).shr(8), 1f - collapseRatio ),
                    mixColorComponents(
                        colorBack.and(0x000000ff), colorPrimary.and(0x000000ff), 1f - collapseRatio )
                )
                imgvSelectPhoto.drawable!!.setTint(transColor)

                val params = (vContent.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
                    width = MATCH_PARENT
                    height = MATCH_PARENT
                } ?: CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                params.topMargin = layout.totalScrollRange + offset
                vContent.layoutParams = params

                wantToolbarCollapsed = false
            }
        } )

        appbarLayout.setExpanded(false, false)    // Initially collapsed.
    }

    private fun applySeminar()
    {
        tbLayout.title = seminar?.name ?: ""
        tvCity.text = seminar?.city ?: ""
        tvAddress.text = seminar?.address ?: ""
        tvContent.text = seminar?.content ?: ""
    }

    private fun applyImage(image: DecodedImage)
    {
        imgvAvatar.setImageBitmap(image.bitmap)
        if(wantToolbarCollapsed) {
            appbarLayout.setExpanded(false, false)
            wantToolbarCollapsed = false
        }
        prbImageLoad.visibility = View.GONE
    }

    private fun applyInfo(info: SeminarInfo)
    {
        tvAppointsCount.text = "${info.appointsCount}"
        // tvTutorsCount = TODO()
        infoLoaded = true
    }

    private fun applyDays()
    {
        if(days == null || days!!.isEmpty()) {
            tvDays.setText(R.string.msg_unknown_date)
            return
        }

        for(day in days!!)
        {
            var text = tvDays.text
            if(text.isNotEmpty())
                text = "$text\n"
            tvDays.text = "$text${DATETIME_FORMAT.format(day.start.time)}"
        }
    }

    private fun applyCosts()
    {
        if(costs == null || costs!!.isEmpty()) {
            tvCosts.setText(R.string.msg_unknown_cost)
            return
        }
        for(cost in costs!!)
        {
            tvCosts.text = "${tvCosts.text}" +
                "${if(tvCosts.text.isEmpty()) "" else "\n"}" +
                "${cost.cost}"
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    private val disposable = CompositeDisposable()
    private var decodeID: Int? = null

    private fun subscribeSeminar(seminarID: Int)
        = super.viewModel.createSeminarObservable(seminarID)
            .doOnNext { sem ->
                seminar = sem
                applySeminar()

                super.viewModel.actionSubject
                    .onNext( SpeechManAction.RequestSeminarInfo(sem) )

                sem.imageUri?.also {
                    decodeID = super.viewModel.nextImageDecodeID
                    super.viewModel.actionSubject
                        .onNext( SpeechManAction.DecodeImages(decodeID!!, listOf(it)) )
                } ?: prbImageLoad.apply {
                    visibility = View.GONE
                }

                updateProgressBarByLoad()
            }.subscribe()

    private fun subscribeDays(seminarID: Int)
        = super.viewModel.createSemDaysObservable(seminarID)
            .doOnNext { semdays ->
                days = semdays
                applyDays()
                updateProgressBarByLoad()
            }.subscribe()

    private fun subscribeCosts(seminarID: Int)
        = super.viewModel.createSemCostsObservable(seminarID)
            .doOnNext { semcosts ->
                costs = semcosts
                applyCosts()
                updateProgressBarByLoad()
            }.subscribe()

    private fun subscribeInfos()
        = super.viewModel.createSeminarInfosObservable()
            .doOnNext { infos ->
                applyInfo(infos[0])
                updateProgressBarByLoad()
            }.subscribe()

    private fun subscribeDecodedImages()
        = super.viewModel.createDecodedImagesObservable()
            .onErrorResumeNext { thr: Throwable ->
                seminar?.also {
                    super.handleImageNotDecoded(thr, decodeID!!, listOf(it)) { it }
                }
                Observable.empty()
            }.doOnNext { image ->
                if(decodeID == image.requestID)
                    applyImage(image)
            }.subscribe()


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        setupToolbar()
        fabAppoints.setOnClickListener { showParticipants() }
        fabEdit.setOnClickListener { navigateEdit() }
        fabMoreSemDays.setOnClickListener {
            navigateLookup(SemDaysCRUPage.CRU_COMPONENT_ID)
        }
        fabMoreSemCosts.setOnClickListener {
            navigateLookup(SemCostsCRUPage.CRU_COMPONENT_ID)
        }
    }

    override fun onStart()
    {
        super.onStart()
        val seminarID = super.getIntArgument(KEY_SEMINAR_ID, null, "KEY_SEMINAR_ID")
        disposable.add(subscribeSeminar(seminarID))
        disposable.add(subscribeDecodedImages())
        disposable.add(subscribeDays(seminarID))
        disposable.add(subscribeCosts(seminarID))
        disposable.add(subscribeInfos())
    }

    override fun onStop()
    {
        super.onStop()
        disposable.clear()
    }
}