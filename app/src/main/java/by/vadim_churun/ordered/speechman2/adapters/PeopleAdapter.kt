package by.vadim_churun.ordered.speechman2.adapters

import androidx.fragment.app.FragmentManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.dests.people.PersonDetailDestination
import by.vadim_churun.ordered.speechman2.dialogs.people.DeletePersonDialog
import by.vadim_churun.ordered.speechman2.model.objects.PersonHeader
import by.vadim_churun.ordered.speechman2.views.SwipeOutListener
import kotlinx.android.synthetic.main.people_listitem.view.*


/** Displays people with number of appointments and orders for each. **/
class PeopleAdapter(
    val context: Context,
    val headers: List<PersonHeader>,
    val navController: NavController,
    val fragmMan: FragmentManager
): RecyclerView.Adapter<PeopleAdapter.PersonViewHolder>() {
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class PersonViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val tvName = itemView.tvName
        val tvAppointsCount = itemView.tvAppointsCount
        val tvOrdersCount = itemView.tvOrdersCount
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // NAVIGATION AND SWIPE OUT:

    private fun navigatePersonDetail(position: Int)
    {
        Bundle().apply {
            putInt(PersonDetailDestination.KEY_PERSON_ID, headers[position].person.ID!!)
        }.also {
            navController.navigate(R.id.actToPersonDetail, it)
        }
    }

    private fun showDeleteDialog(position: Int)
    {
        val args = Bundle()
        args.putInt(DeletePersonDialog.KEY_PERSON_ID, headers[position].person.ID!!)
        DeletePersonDialog().apply {
            arguments = args
            show(fragmMan, null)
        }
    }

    val swipeoutListener = SwipeOutListener().apply {
        onSwipeOut = object: SwipeOutListener.OnSwipeOutCallback {
            override fun onSwipeOut(vh: RecyclerView.ViewHolder) {
                showDeleteDialog(vh.adapterPosition)
            }
        }
    }

    val swipeoutDetector = object: GestureDetector(context, swipeoutListener) {
        override fun onTouchEvent(ev: MotionEvent): Boolean
        {
            if(ev.actionMasked == MotionEvent.ACTION_UP
                || ev.actionMasked == MotionEvent.ACTION_CANCEL)
                swipeoutListener.onUp()
            return super.onTouchEvent(ev)
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION

    override fun getItemCount(): Int = headers.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder
        = PersonViewHolder(
            LayoutInflater.from(context).inflate(R.layout.people_listitem, parent, false) )

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        fun Int?.toCountString(stringID: Int)
            = this?.run {
                context.getString(stringID, this)
            } ?: ""

        val head = headers[position]
        holder.tvName.text = head.person.name
        holder.tvAppointsCount.text = head.countAppoints.toCountString(R.string.fs_appoints_count)
        holder.tvOrdersCount.text = head.countOrders?.toCountString(R.string.fs_orders_count)

        holder.itemView.setOnClickListener {
            navigatePersonDetail(position)
        }
        holder.itemView.setOnTouchListener { v, event ->
            swipeoutListener.targetHolder = holder
            swipeoutDetector.onTouchEvent(event)
        }
    }
}