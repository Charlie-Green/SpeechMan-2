package by.vadim_churun.ordered.speechman2.adapters

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.objs.SeminarHeader
import by.vadim_churun.ordered.speechman2.dialogs.associations.AddAppointmentDialog
import by.vadim_churun.ordered.speechman2.dialogs.associations.AppointmentDialog
import by.vadim_churun.ordered.speechman2.model.objects.DecodedImage
import kotlinx.android.synthetic.main.person_potential_appoint_listitem.view.*
import java.text.SimpleDateFormat


class PersonPotentialAppointsAdapter(val context: Context,
    val headers: List<SeminarHeader>,
    val personID: Int,
    val fragmMan: FragmentManager
): RecyclerView.Adapter<PersonPotentialAppointsAdapter.SeminarViewHolder>()
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        // TODO: Localize.
        private val DATETIME_FORMAT = SimpleDateFormat("dd.MM.yyyy, HH:mm")
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class SeminarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val frltBackground = itemView.frltBackground
        val imgvAvatar = itemView.imgvAvatar
        val tvName = itemView.tvName
        val tvStart = itemView.tvStart
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // NAVIGATION:

    private fun showAddAppointDialog(position: Int)
    {
        val dialogArgs = Bundle()
        dialogArgs.putInt(AppointmentDialog.KEY_PERSON_ID, personID)
        dialogArgs.putInt(AppointmentDialog.KEY_SEMINAR_ID, headers[position].ID!!)
        AddAppointmentDialog().apply {
            arguments = dialogArgs
            show(fragmMan, null)
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONTENT UPDATE:

    private val images = Array<Bitmap?>(headers.size) { null }

    fun setAvatar(avatar: DecodedImage)
    {
        images[avatar.listPosition] = avatar.bitmap
        super.notifyItemChanged(avatar.listPosition)
    }

    fun flick(holder: SeminarViewHolder)
    {
        AnimatorInflater.loadAnimator(context, R.animator.flick).apply {
            setTarget(holder.frltBackground)
            start()
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION:

    override fun getItemCount(): Int
        = headers.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(context)
            .inflate(R.layout.person_potential_appoint_listitem, parent, false)
            .let { SeminarViewHolder(it) }

    override fun onBindViewHolder(holder: SeminarViewHolder, position: Int)
    {
        holder.tvName.text = headers[position].name
        holder.tvStart.text = headers[position].start?.let {
            DATETIME_FORMAT.format(it.timeInMillis)
        } ?: context.getString(R.string.msg_unknown_date)
        images[position]?.also {
            holder.imgvAvatar.setImageBitmap(it)
        } ?: holder.imgvAvatar.setImageResource(R.drawable.img_default_avatar)

        holder.frltBackground.animation?.cancel()
        holder.itemView.setOnClickListener {
            flick(holder)
            showAddAppointDialog(position)
        }
    }
}