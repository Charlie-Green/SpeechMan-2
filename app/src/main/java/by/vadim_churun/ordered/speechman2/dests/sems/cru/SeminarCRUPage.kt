package by.vadim_churun.ordered.speechman2.dests.sems.cru

import android.content.Context
import android.util.AttributeSet
import by.vadim_churun.ordered.speechman2.model.objects.SeminarBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import by.vadim_churun.ordered.speechman2.dests.CRUView


/** Specification of [CRUView] where the "shared object" is a [SeminarBuilder] instance. **/
abstract class SeminarCRUPage: CRUView
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var builder: SeminarBuilder? = null

    private fun subscribeBuilder()
        = this.viewModel.createSeminarBuilderObservable()
            .doOnNext { newBuilder ->
                builder = newBuilder
                onApplyBuilder(newBuilder)
            }.subscribe()


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ABSTRACTIONS:

    /** All actual information from the UI needs to be written to this [SeminarBuilder] object. **/
    protected abstract fun onWriteChanges(dest: SeminarBuilder)

    /** This {@link SeminarBuilder} needs to be displayed. **/
    protected abstract fun onApplyBuilder(builder: SeminarBuilder)

    override fun onCommitChanges()
    {
        val mBuilder = builder ?: return
        onWriteChanges(mBuilder)
        this.viewModel.actionSubject
            .onNext( SpeechManAction.PublishSeminarBuilder(mBuilder) )
    }

    override fun onSaveObject()
    {
        val mBuilder = builder ?: return
        onWriteChanges(mBuilder)
        this.viewModel.actionSubject
            .onNext( SpeechManAction.SaveSeminar(mBuilder) )
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
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
        disposable.add(subscribeBuilder())
    }

    override fun onDetachedFromWindow()
    {
        disposable.clear()
        super.onDetachedFromWindow()
    }
}