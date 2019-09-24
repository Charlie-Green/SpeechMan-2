package by.vadim_churun.ordered.speechman2.adapters

import androidx.fragment.app.FragmentManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.objs.AppointedSeminar
import by.vadim_churun.ordered.speechman2.dests.people.PersonAppointsListDestination
import by.vadim_churun.ordered.speechman2.dialogs.people.DeletePersonAppointDialog
import by.vadim_churun.ordered.speechman2.dialogs.associations.*
import by.vadim_churun.ordered.speechman2.model.objects.DecodedImage
import by.vadim_churun.ordered.speechman2.views.SwipeOutListener
import kotlinx.android.synthetic.main.person_appoints_listitem.view.*


class AppointedSeminarsAdapter(val context: Context,
    val items: List<AppointedSeminar>,
    val fragmMan: FragmentManager
): RecyclerView.Adapter<AppointedSeminarsAdapter.ASViewHolder>()
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEWHOLDER:

    class ASViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val imgvSeminatImage = itemView.imgvSeminarImage
        val tvSeminarName = itemView.tvSeminarName
        val tvPurchase = itemView.tvPurchase
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NAVIGATION:

    private fun navigateEditAppoint(position: Int)
    {
        val dialogArgs = Bundle()
        dialogArgs.putInt(AppointmentDialog.KEY_PERSON_ID, items[position].appoint.personID)
        dialogArgs.putInt(AppointmentDialog.KEY_SEMINAR_ID, items[position].appoint.seminarID)
        EditAppointmentDialog().apply {
            arguments = dialogArgs
            show(fragmMan, null)
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // APPLYING IMAGES:

    private val avatars = Array<Bitmap?>(items.size) { null }

    fun setImage(image: DecodedImage)
    {
        android.util.Log.i(PersonAppointsListDestination::class.java.simpleName, "Setting image at ${image.listPosition}")
        avatars[image.listPosition] = image.bitmap
        super.notifyItemChanged(image.listPosition)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SWIPE OUT

    private val swipeoutListener = SwipeOutListener().apply {
        this.onSwipeOut = object: SwipeOutListener.OnSwipeOutCallback {
            override fun onSwipeOut(vh: RecyclerView.ViewHolder)
            {
                val appoint = items[vh.adapterPosition].appoint
                val dialogArgs = Bundle()
                dialogArgs.putInt(DeletePersonAppointDialog.KEY_PERSON_ID, appoint.personID)
                dialogArgs.putInt(DeletePersonAppointDialog.KEY_SEMINAR_ID, appoint.seminarID)
                DeletePersonAppointDialog().apply {
                    arguments = dialogArgs
                    show(fragmMan, null)
                }
            }
        }
    }

    private val swipeoutDetector = GestureDetector(context, swipeoutListener)


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION:

    override fun getItemCount(): Int
        = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(context)
            .inflate(R.layout.person_appoints_listitem, parent, false)
            .let { ASViewHolder(it) }

    override fun onBindViewHolder(holder: ASViewHolder, position: Int)
    {
        holder.tvSeminarName.text = items[position].seminar.name
        holder.tvPurchase.text = context.getString(
            R.string.fs_appoint_money,
            items[position].appoint.purchase,
            items[position].appoint.cost
        )

        val avatar = avatars[position]
        if(avatar == null)
            holder.imgvSeminatImage.setImageResource(R.drawable.img_default_avatar)
        else
            holder.imgvSeminatImage.setImageBitmap(avatar)

        holder.itemView.setOnClickListener {
            navigateEditAppoint(position)
        }

        holder.itemView.setOnTouchListener { v, event ->
            swipeoutListener.targetHolder = holder
            if(event.actionMasked == MotionEvent.ACTION_UP ||
                event.actionMasked == MotionEvent.ACTION_CANCEL )
                swipeoutListener.onUp()
            return@setOnTouchListener swipeoutDetector.onTouchEvent(event)
        }
    }
}