package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.model.objects.SeminarBuilder
import by.vadim_churun.ordered.speechman2.viewmodel.SpeechManAction
import by.vadim_churun.ordered.speechman2.views.DialogFactory
import java.util.Calendar
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.semdays_listitem.view.*
import java.text.SimpleDateFormat


class SemDaysAdapter(val context: Context,
    var builder: SeminarBuilder,
    val actionSubject: PublishSubject<SpeechManAction>
): RecyclerView.Adapter<SemDaysAdapter.SemDayViewHolder>()
{
    //////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        // TODO: Localize.
        private val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy")
        private val TIME_FORMAT = SimpleDateFormat("HH:mm")
        private const val MILLIS_PER_MINUTE = 60000
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class SemDayViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val tvDate      = itemView.tvDate
        val tvTimeStart = itemView.tvTimeStart
        val tvTimeEnd   = itemView.tvTimeEnd
        val imgvDelete  = itemView.imgvDelete
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // UPDATING DATA:

    private var editable = true
    var isEditable: Boolean
        get() = editable
        set(value) {
            editable = value
            super.notifyDataSetChanged()
        }


    //////////////////////////////////////////////////////////////////////////////////////////////////
    // ADAPTER METHODS IMPLEMENTATION:

    override fun getItemCount(): Int
        = builder.dayBuilders.size + 1     // +1 for the header.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemDayViewHolder
        = LayoutInflater.from(context)
            .inflate(R.layout.semdays_listitem, parent, false).let {
                SemDayViewHolder(it)
            }

    override fun onBindViewHolder(holder: SemDayViewHolder, position: Int)
    {
        holder.tvDate.isEnabled = (position != 0)
        holder.tvTimeStart.isEnabled = (position != 0)
        holder.tvTimeEnd.isEnabled = (position != 0)

        if(position == 0) {
            holder.tvDate.setText(R.string.la_semday_date)
            holder.tvTimeStart.setText(R.string.la_semday_start)
            holder.tvTimeEnd.setText(R.string.la_semday_end)
            holder.imgvDelete.visibility = View.INVISIBLE
            return
        }

        val dayBuilder = builder.dayBuilders[position - 1]
        val date = dayBuilder.start.time
        holder.tvDate.text = DATE_FORMAT.format(date)
        holder.tvTimeStart.text = TIME_FORMAT.format(date)
        date.time += dayBuilder.duration * MILLIS_PER_MINUTE
        holder.tvTimeEnd.text = TIME_FORMAT.format(date)

        if(editable) {
            holder.tvDate.setOnClickListener {
                DialogFactory.setNextSuggestion(dayBuilder.start)
                DialogFactory.pickDate(context) { day, month, year ->
                    dayBuilder.start.set(year, month, day)
                    actionSubject.onNext( SpeechManAction.PublishSeminarBuilder(builder) )
                }
            }
        } else {
            holder.tvDate.setOnClickListener(null)
        }

        if(editable) {
            holder.tvTimeStart.setOnClickListener {
                DialogFactory.setNextSuggestion(dayBuilder.start)
                DialogFactory.pickTime(context) { hour, min ->
                    dayBuilder.start.set(Calendar.HOUR_OF_DAY, hour)
                    dayBuilder.start.set(Calendar.MINUTE, min)
                    actionSubject.onNext( SpeechManAction.PublishSeminarBuilder(builder) )
                }
            }
        } else {
            holder.tvTimeStart.setOnClickListener(null)
        }

        if(editable) {
            holder.tvTimeEnd.setOnClickListener {
                DialogFactory.setNextSuggestion(dayBuilder.start, dayBuilder.duration.toInt())
                DialogFactory.pickTime(context) { hour, min ->
                    val end = dayBuilder.start.clone() as Calendar
                    end.set(Calendar.HOUR_OF_DAY, hour)
                    end.set(Calendar.MINUTE, min)
                    if(end.before(dayBuilder.start))
                        end.add(Calendar.DAY_OF_MONTH, 1)
                    dayBuilder.duration = (end.timeInMillis - dayBuilder.start.timeInMillis)
                        .div(MILLIS_PER_MINUTE)
                        .toShort()
                    actionSubject.onNext( SpeechManAction.PublishSeminarBuilder(builder) )
                }
            }
        } else {
            holder.tvTimeEnd.setOnClickListener(null)
        }

        if(editable) {
            holder.imgvDelete.visibility = View.VISIBLE
            holder.imgvDelete.setOnClickListener {
                builder.dayBuilders.removeAt(position - 1)
                actionSubject.onNext( SpeechManAction.PublishSeminarBuilder(builder) )
            }
        } else {
            holder.imgvDelete.visibility = View.INVISIBLE
        }
    }
}