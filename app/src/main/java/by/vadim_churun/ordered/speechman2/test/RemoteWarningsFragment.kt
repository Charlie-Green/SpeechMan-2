package by.vadim_churun.ordered.speechman2.test

import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.model.objects.RemoteData


class RemoteWarningsFragment: TestFragment()
{
    override fun getAdapter(data: RemoteData): Pair<RecyclerView.Adapter<*>, String>
        = Pair(
            RemoteWarningsAdapter(super.requireContext(), data.warnings),
            "Warnings: ${data.warnings.size}"
        )
}