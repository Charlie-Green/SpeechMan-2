package by.vadim_churun.ordered.speechman2.test

import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.model.objects.RemoteData


class RemoteLacksFragment: TestFragment()
{
    override fun getAdapter(data: RemoteData): Pair<RecyclerView.Adapter<*>, String>
        = Pair(
            RemoteLacksAdapter(super.requireContext(), data.lacks),
            "Lacks: ${data.lacks.size}"
        )
}