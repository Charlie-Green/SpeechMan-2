package by.vadim_churun.ordered.speechman2.dests.sems.cru

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.model.objects.*
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.seminar_image_cru_page.view.*
import kotlinx.android.synthetic.main.seminars_listitem.view.imgvAvatar


/** Provides read and update of general information about a [Seminar]. **/
class SeminarImageCRUPage: SeminarCRUPage
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        /** This [View] receives CRU-related actions with componentID set to this value. **/
        val CRU_COMPONENT_ID = 1
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // NEW:

    private var decodeID: Int? = null
    private var lastUri: Uri? = null

    private fun checkupSelectedImage()
    {
        val action = super.viewModel.consumeKeptAction() ?: return
        if(action !is SpeechManAction.DecodeImages
            || action.imagesSource.isEmpty()
            || action.imagesSource[0] !is Uri) {
            super.viewModel.keepAction(action)
            return
        }
        decodeID = action.requestID
        super.viewModel.actionSubject.onNext(action)
        lastUri = action.imagesSource[0] as Uri
    }

    private fun requestDecodeImage(uri: Uri)
    {
        prbAvatarLoad.visibility = View.VISIBLE
        decodeID = super.viewModel.nextImageDecodeID
        super.viewModel.actionSubject
            .onNext( SpeechManAction.DecodeImages(decodeID!!, listOf(uri)) )
    }

    private fun subscribeDecodedImages(): Disposable
        = super.viewModel.createDecodedImagesObservable()
            .onErrorResumeNext { observer: Observer<in DecodedImage> ->
                super.getContext().getString(R.string.msg_image_not_decoded).let {
                    SpeechManAction.ShowMessage(true,it)
                }.also {
                    super.viewModel.actionSubject.onNext(it)
                }
                lastUri = null; imgvAvatar.setImageResource(R.drawable.img_default_avatar)
                prbAvatarLoad.visibility = View.GONE
                disposable.clear()
                disposable.add(subscribeDecodedImages())
            }.doOnNext { image ->
                if(image.requestID == decodeID) {
                    imgvAvatar.setImageBitmap(image.bitmap)
                    prbAvatarLoad.visibility = View.GONE
                }
            }.subscribe()


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // OVERRIDDEN:

    override val layoutResID: Int
        get() = R.layout.seminar_image_cru_page


    override fun onApplyBuilder(builder: SeminarBuilder)
    {
        prbAvatarLoad.visibility = View.GONE
        lastUri = builder.imageUri     // Pick a Uri from builder.
        checkupSelectedImage()         // Possibly override this value with a Uri the user selected.
        lastUri?.also { requestDecodeImage(it) }
            ?: imgvAvatar.setImageResource(R.drawable.img_default_avatar)
    }

    override fun onEditableChanged(isEditable: Boolean)
    {
        buClear.isVisible = isEditable
        buSelect.isVisible = isEditable
    }

    override fun onWriteChanges(dest: SeminarBuilder)
    {
        dest.imageUri = lastUri
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    constructor(context: Context):
        super(context)
    constructor(context: Context, attrs: AttributeSet?):
        super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
        super(context, attrs, defStyleAttr)


    override val componentID: Int
        get() = SeminarImageCRUPage.CRU_COMPONENT_ID


    override fun onWindowFocusChanged(hasWindowFocus: Boolean)
    {
        super.onWindowFocusChanged(hasWindowFocus)
        if(hasWindowFocus) {
            checkupSelectedImage()
            lastUri?.also { requestDecodeImage(it) }
        }
    }

    override fun onAttachedToWindow()
    {
        super.onAttachedToWindow()
        super.disposable.add(subscribeDecodedImages())

        buSelect.setOnClickListener {
            super.viewModel.actionSubject
                .onNext(SpeechManAction.SelectImage())
        }

        buClear.setOnClickListener {
            imgvAvatar.setImageResource(R.drawable.img_default_avatar)
            lastUri = null
        }
    }
}