package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.entities.Person
import by.vadim_churun.ordered.speechman2.model.objects.PersonInfo
import kotlinx.android.synthetic.main.people_listitem.view.*


/** Displays people with number of appointments and orders for each. **/
class PeopleAdapter(val context: Context, val people: List<Person>):
    RecyclerView.Adapter<PeopleAdapter.PersonViewHolder>()
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class PersonViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val tvName = itemView.tvName
        val tvAppointsCount = itemView.tvAppointsCount
        val tvOrdersCount = itemView.tvOrdersCount
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // DYNAMIC CONTENT

    private var infos = MutableList<PersonInfo?>(people.size) { null }

    fun setPersonInfo(info: PersonInfo)
    {
        infos[info.listPosition] = info
        super.notifyItemChanged(info.listPosition)
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION

    override fun getItemCount(): Int = people.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder
        = PersonViewHolder(
            LayoutInflater.from(context).inflate(R.layout.people_listitem, parent, false) )

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int)
    {
        holder.tvName.text = people[position].name

        val info = infos[position]
        if(info == null) {
            holder.tvAppointsCount.text = ""
            holder.tvOrdersCount.text = ""
        } else {
            holder.tvAppointsCount.text = context
                .getString(R.string.fs_appoints_count, info.countAppoints)
            holder.tvOrdersCount.text = context
                .getString(R.string.fs_orders_count, info.countOrders)
        }
    }
}