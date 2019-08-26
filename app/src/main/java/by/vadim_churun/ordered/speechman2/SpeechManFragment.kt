package by.vadim_churun.ordered.speechman2

import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManViewModel


abstract class SpeechManFragment(
    protected val layoutResID: Int
): DialogFragment()
{
    private var vm: SpeechManViewModel? = null

    protected val viewModel: SpeechManViewModel
        get() {
            return vm ?: ViewModelProviders
                .of(super.requireActivity())
                .get(SpeechManViewModel::class.java)
                .also { vm = it }
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


    override fun onCreateView
    (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        = inflater.inflate(layoutResID, container, false)
}