package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.entities.Person
import by.vadim_churun.ordered.speechman2.model.objects.SeminarAppointsBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.edit_participants_listitem.view.*


class EditParticipantsAdapter(val context: Context,
    val displayedPeople: List<Person>,
    val builder: SeminarAppointsBuilder,
    val actionChannel: PublishSubject<SpeechManAction>
): RecyclerView.Adapter<EditParticipantsAdapter.ParticipantViewHolder>()
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class ParticipantViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val tvName = itemView.tvName
        val tvType = itemView.tvType
        val chbIsAppointed = itemView.chbIsAppointed
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION:

    override fun getItemCount(): Int
        = displayedPeople.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
        = LayoutInflater.from(context)
            .inflate(R.layout.edit_participants_listitem, parent, false)
            .let { ParticipantViewHolder(it) }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int)
    {
        val person = displayedPeople[position]
        holder.tvName.text = person.name
        // TODO: Display the person's type.
        holder.chbIsAppointed.isChecked = builder.willBeAppointed(person.ID!!)

        holder.chbIsAppointed.setOnClickListener {
            builder.swapAppointingStatus(person.ID!!)
            actionChannel.onNext( SpeechManAction.ApplySemAppointsBuilderChange() )
        }
    }
}