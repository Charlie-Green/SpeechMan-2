package by.vadim_churun.ordered.speechman2.test

import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.model.objects.RemoteData


class RemoteEntitiesFragment: TestFragment()
{
    override fun getAdapter(data: RemoteData): Pair<RecyclerView.Adapter<*>, String>
        = Pair(
            RemoteEntitiesAdapter(super.requireContext(), data.entities),
            "Entities: ${data.entities.size}"
        )
}