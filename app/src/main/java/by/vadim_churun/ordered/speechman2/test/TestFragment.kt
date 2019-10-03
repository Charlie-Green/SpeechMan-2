package by.vadim_churun.ordered.speechman2.test

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.model.objects.RemoteData
import kotlinx.android.synthetic.main.test_fragment.*


abstract class TestFragment: Fragment()
{
    protected abstract fun getAdapter(data: RemoteData): Pair<RecyclerView.Adapter<*>, String>

    fun applyData(data: RemoteData)
    {
        recv.layoutManager = recv.layoutManager
            ?: LinearLayoutManager(super.requireContext())
        val adapterPair = getAdapter(data)
        recv.swapAdapter(adapterPair.first, true)
        tvCount.text = adapterPair.second
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
        = inflater.inflate(R.layout.test_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    { applyData(TestRepository.data) }
}