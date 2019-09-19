package by.vadim_churun.ordered.speechman2

import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import by.vadim_churun.ordered.speechman2.db.entities.Seminar
import by.vadim_churun.ordered.speechman2.model.exceptions.ImageNotDecodedException
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManViewModel


abstract class SpeechManFragment(
    private val layoutResID: Int
): DialogFragment()
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // HELP PROPERTIES AND METHODS:

    private var vm: SpeechManViewModel? = null

    protected val viewModel: SpeechManViewModel
        get() {
            return vm ?: ViewModelProviders
                .of(super.requireActivity())
                .get(SpeechManViewModel::class.java)
                .also { vm = it }
        }


    /** Retrieves an integer argument from the current fragment's argument bundle.
      * @param key bundle key
      * @param defaultValue 1)if not null and the argument is missing, this value is returned;
      *     2)if null, the argument is considered required and,
      *     in case it's missing, an exception is raised up.
      * @param argName if an exception is thrown, this string is used
      * to represent the missing argument's name. **/
    protected fun getIntArgument(key: String, defaultValue: Int?, argName: String): Int
    {
        val args = super.getArguments()
        if(args == null)
            return defaultValue ?: throw Exception("Missing arguments. ")
        if(args.containsKey(key)) return args.getInt(key)
        return defaultValue ?: throw Exception(
            "Missing ${if(argName == null) "an argument" else "the $argName argument"}" )
    }


    protected fun setupSearchViewLayoutBehaviour
    (searchView: SearchView, vararg overlappedViews: View)
    {
        fun setViewsVisibility(newVisibility: Int)
        {
            for(v in overlappedViews)
            {
                v.visibility = newVisibility
            }
        }

        fun setSearchViewWidth(widthConstant: Int)
        {
            searchView.layoutParams = searchView.layoutParams?.apply {
                width = widthConstant
            } ?: ViewGroup.LayoutParams(widthConstant, WRAP_CONTENT)
        }

        searchView.setOnSearchClickListener {
            setViewsVisibility(View.GONE)
            setSearchViewWidth(MATCH_PARENT)
        }

        searchView.setOnCloseListener {
            setSearchViewWidth(WRAP_CONTENT)
            setViewsVisibility(View.VISIBLE)
            false    // Do not override the default behaviour.
        }
    }

    protected fun <T> handleImageNotDecoded
    (thr: Throwable, expectedRequestID: Int, list: List<T>, getSeminar: (T) -> Seminar)
    {
        if(thr !is ImageNotDecodedException ||
            thr.requestID != expectedRequestID )
            return
        val badSem = getSeminar(list[thr.listPosition])
        val message = super.getString(R.string.fs_seminar_image_not_decoded, badSem.name)
        val goodSem = Seminar(badSem.ID,
            badSem.name,
            badSem.city,
            badSem.address,
            badSem.content,
            null,    // Remove the Uri we cannot decode.
            badSem.costing,
            badSem.isLogicallyDeleted
        )
        this.viewModel.actionSubject.apply {
            onNext( SpeechManAction.ShowMessage(true, message) )
            onNext( SpeechManAction.UpdateSeminar(goodSem) )
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // LIFECYCLE:

    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater.inflate(layoutResID, container, false)
}