package by.vadim_churun.ordered.speechman2.repo

import android.content.Context
import android.net.Uri
import by.vadim_churun.ordered.speechman2.db.objs.*
import by.vadim_churun.ordered.speechman2.model.exceptions.ImageNotDecodedException
import by.vadim_churun.ordered.speechman2.model.objects.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class BitmapRepository(appContext: Context): SpeechManRepository(appContext)
{
    class DecodeRequest(
        val id: Int,
        val source: List<Any>
    )


    companion object
    {
        private val LOGTAG = BitmapRepository::class.java.simpleName

        private var idNextRequest = 0
        val nextRequestID
            get() = synchronized(this) {
                idNextRequest++
            }

        private val canceledRequestIDs = HashSet<Int>()
        fun cancelRequest(requestID: Int)
            = synchronized(this) {
                canceledRequestIDs.add(requestID)
            }
        private fun isRequestCanceled(requestID: Int): Boolean
            = synchronized(this) {
                canceledRequestIDs.contains(requestID).also {
                    canceledRequestIDs.remove(requestID)
                }
            }
    }


    val sourceSubject = PublishSubject.create<DecodeRequest>()


    fun createDecodedImagesObservable()
        = sourceSubject
            .observeOn(Schedulers.single())
            .switchMap { request ->
                Observable.create<DecodedImage> { emitter ->
                    for(j in 0..request.source.lastIndex)
                    {
                        val item = request.source[j]

                        var uri: Uri?
                        when(item)
                        {
                            is AppointedSeminar -> { uri = item.seminar.imageUri }
                            is SeminarBuilder   -> { uri = item.imageUri }
                            is SeminarHeader    -> { uri = item.imageUri }
                            is Uri              -> { uri = item }
                            else -> {
                                throw IllegalArgumentException(
                                    "Not sure how to convert ${item.javaClass.name} to ${Uri::javaClass.name}" )
                            }
                        }

                        if(isRequestCanceled(request.id)) break
                        val img: DecodedImage?
                        try {
                            img = DecodedImage.from(request.id, super.appContext.contentResolver, j, uri)
                        } catch(exc: Exception) {
                            throw ImageNotDecodedException(request.id, j, uri!!)
                        }
                        img?.also { emitter.onNext(it) }
                    }
                    emitter.onComplete()
                }
            }.observeOn(AndroidSchedulers.mainThread())
}