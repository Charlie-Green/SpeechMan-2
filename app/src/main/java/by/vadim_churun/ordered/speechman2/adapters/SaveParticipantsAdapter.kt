package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.entities.Appointment
import by.vadim_churun.ordered.speechman2.db.objs.Money
import by.vadim_churun.ordered.speechman2.model.objects.SeminarAppointsBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.save_participants_listitem.view.*


class SaveParticipantsAdapter(val context: Context,
    val builder: SeminarAppointsBuilder,
    val actionChannel: Subject<SpeechManAction>
): RecyclerView.Adapter<SaveParticipantsAdapter.ParticipantViewHolder>()
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class ParticipantViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val tvName = itemView.tvName
        val etCost = itemView.etCost
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // APPLYING USER INPUT:

    private val costInputs = Array<CharSequence?>(builder.addedAppoints.size) { null }

    /** Parses costs entered by the user and writes them to [builder].
      * @return list of positions where input is incorrect. **/
    fun commitCosts(): List<Int>
    {
        val errors = emptyList<Int>().toMutableList()

        for(j in 0.until(builder.addedAppoints.size))
        {
            val input = costInputs[j] ?: continue
            val oldAppoint = builder.addedAppoints[j]
            val newAppoint: Appointment
            try {
                val newCost = Money.parse(input.toString())
                newAppoint = Appointment(oldAppoint.personID, oldAppoint.seminarID,
                    oldAppoint.purchase, newCost,
                    oldAppoint.historyStatus, false )
            } catch(exc: Exception) {
                errors.add(j)
                continue
            }
            builder.updateAppointment(newAppoint)
        }

        actionChannel.onNext( SpeechManAction.PublishSemAppointsBuilder(builder) )
        return errors
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION

    override fun getItemCount(): Int
        = builder.addedAppoints.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(context)
            .inflate(R.layout.save_participants_listitem, parent, false)
            .let { ParticipantViewHolder(it) }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int)
    {
        val appoint = builder.addedAppoints[position]
        // TODO: Optimize this.
        val person = builder.allPeople.find { it.ID == appoint.personID }!!
        holder.tvName.text = person.name
        holder.etCost.setText(appoint.cost.toString())
        holder.etCost.addTextChangedListener( object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int)
            {   }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {   }

            override fun afterTextChanged(s: Editable)
            {
                costInputs[position] = s
            }
        } )
    }
}