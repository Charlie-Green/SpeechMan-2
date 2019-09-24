package by.vadim_churun.ordered.speechman2.dests

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import by.vadim_churun.ordered.speechman2.viewmodel.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable


/** The common abstraction for a [View]
  * to allow reading and updating some object shared with other components. **/
abstract class CRUView: FrameLayout
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTIONALITY:

    private var model: SpeechManViewModel? = null
    protected val viewModel: SpeechManViewModel
        get() {
            return model ?:
            ViewModelProviders.of(super.getContext() as AppCompatActivity)
                .get(SpeechManViewModel::class.java)
                .also { model = it }
        }


    /** All CRU-related {@link SpeechManAction}s whose componentID is not the one
      * returned by this property will be filtered out.**/
    protected abstract val componentID: Int

    /** Opportunity to change the shared object via this updater changes. **/
    protected abstract fun onEditableChanged(isEditable: Boolean)

    /** Changes made locally need to be published
     * and any other interested component should be notified of these changes. **/
    protected abstract fun onCommitChanges()

    /** The object with its last changes needs to be persisted. **/
    protected abstract fun onSaveObject()


    private fun parseAction(action: SpeechManAction)
    {
        when(action)
        {
            is SpeechManAction.ChangeCRUEditable -> {
                if(action.componentID == this.componentID)
                    onEditableChanged(action.isEditable)
            }

            is SpeechManAction.CommitCRUObject -> {
                if(action.componentID == this.componentID)
                    onCommitChanges()
            }

            is SpeechManAction.SaveCRUObject -> {
                if(action.componentID == this.componentID)
                    onSaveObject()
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // RX:

    /** Any subscription added here is cleared when the [View] is detached. **/
    protected val disposable = CompositeDisposable()

    private fun subscribeAction()
        = this.viewModel.actionSubject
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { action ->
                parseAction(action)
            }.subscribe()


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    abstract val layoutResID: Int

    constructor(context: Context):
        super(context) { initCRU() }
    constructor(context: Context, attrs: AttributeSet?):
        super(context, attrs) { initCRU() }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
        super(context, attrs, defStyleAttr) { initCRU() }

    private fun initCRU()
    {
        LayoutInflater.from(super.getContext())
            .inflate(layoutResID, this, true)
    }

    override fun onAttachedToWindow()
    {
        super.onAttachedToWindow()
        this.viewModel.consumeKeptAction()?.also {
            this.viewModel.keepAction(it)   // For the other components CRUing the same object.
            parseAction(it)
        }
        disposable.add(subscribeAction())
    }

    override fun onDetachedFromWindow()
    {
        onCommitChanges()
        disposable.clear()
        super.onDetachedFromWindow()
    }
}