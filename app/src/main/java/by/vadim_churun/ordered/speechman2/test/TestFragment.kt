package by.vadim_churun.ordered.speechman2.test

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
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
}