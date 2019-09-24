package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.objs.Participant
import by.vadim_churun.ordered.speechman2.dialogs.associations.AppointmentDialog
import by.vadim_churun.ordered.speechman2.dialogs.associations.EditAppointmentDialog
import kotlinx.android.synthetic.main.participants_listitem.view.*


class ParticipantsAdapter(val context: Context,
    val participants: List<Participant>,
    val fragmMan: FragmentManager
): RecyclerView.Adapter<ParticipantsAdapter.ParticipantViewHolder>()
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class ParticipantViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val tvPersonName = itemView.tvPersonName
        val tvMoney = itemView.tvMoney
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // DYNAMIC OPTIONS:

    private fun navigateEditAppoint(personID: Int, seminarID: Int)
    {
        val dialogArgs = Bundle()
        dialogArgs.putInt(AppointmentDialog.KEY_PERSON_ID, personID)
        dialogArgs.putInt(AppointmentDialog.KEY_SEMINAR_ID, seminarID)
        EditAppointmentDialog().apply {
            arguments = dialogArgs
            show(fragmMan, null)
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION

    override fun getItemCount(): Int = participants.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = ParticipantViewHolder(
            LayoutInflater.from(context).inflate(R.layout.participants_listitem, parent, false)
        )

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int)
    {
        holder.tvPersonName.text = participants[position].person.name
        val appoint = participants[position].appoint
        holder.tvMoney.text = context.getString(
            R.string.fs_appoint_money, appoint.purchase, appoint.cost )
        holder.itemView.setOnClickListener {

            navigateEditAppoint(appoint.personID, appoint.seminarID)
        }
    }
}