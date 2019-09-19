package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.db.objs.SeminarHeader
import by.vadim_churun.ordered.speechman2.dests.sems.SeminarDetailDestination
import by.vadim_churun.ordered.speechman2.dialogs.sems.SeminarDeleteDialog
import by.vadim_churun.ordered.speechman2.model.objects.*
import by.vadim_churun.ordered.speechman2.views.SwipeOutListener
import kotlinx.android.synthetic.main.seminars_listitem.view.*
import java.text.SimpleDateFormat


class SeminarsAdapter(val context: Context,
    val headers: List<SeminarHeader>,
    val navController: NavController,
    val fragmMan: FragmentManager
): RecyclerView.Adapter<SeminarsAdapter.SeminarViewHolder>()
{
    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMPANION OBJECT:

    companion object
    {
        // TODO: Localize it.
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy")
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // VIEW HOLDER:

    class SeminarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val imgvAvatar = itemView.imgvAvatar
        val tvName = itemView.tvName
        val tvDate = itemView.tvDate
        val tvCity = itemView.tvCity
        val tvAppointsCount = itemView.tvAppointsCount
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // DYNAMIC MODIFICATIONS:

    private var infos = List<SeminarInfo?>(headers.size) { null }
    var seminarInfos: List<SeminarInfo?>
        get() { return infos }
        set(value) {
            if(value.size != headers.size)
                throw IllegalArgumentException(
                    "Wrong list size: got ${value.size}, expected ${headers.size}" )
            infos = value
            super.notifyDataSetChanged()
        }


    private var avatars = MutableList<Bitmap?>(headers.size) { null }
    fun setAvatar(avatar: DecodedImage)
    {
        avatars[avatar.listPosition] = avatar.bitmap
        super.notifyItemChanged(avatar.listPosition)
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    // NAVIGATION AND SWIPE OUT:

    private fun navigateSeminarDetail(position: Int)
    {
        Bundle().apply {
            putInt(SeminarDetailDestination.KEY_SEMINAR_ID, headers[position].ID)
        }.also {
            navController.navigate(R.id.actToSeminarDetail, it)
        }
    }

    private fun showDeleteDialog(position: Int)
    {
        val dialogArgs = Bundle()
        dialogArgs.putInt(SeminarDeleteDialog.KEY_SEMINAR_ID, headers[position].ID)
        SeminarDeleteDialog().apply {
            arguments = dialogArgs
            show(fragmMan, null)
        }
    }

    val swipeoutListener = SwipeOutListener().apply {
        onSwipeOut = object: SwipeOutListener.OnSwipeOutCallback {
            override fun onSwipeOut(vh: RecyclerView.ViewHolder)
                = showDeleteDialog(vh.adapterPosition)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeminarViewHolder
            = SeminarViewHolder(
        LayoutInflater.from(context).inflate(R.layout.seminars_listitem, parent, false) )

    override fun onBindViewHolder(holder: SeminarViewHolder, position: Int)
    {
        holder.tvName.text = headers[position].name
        holder.tvCity.text = headers[position].city
        holder.tvAppointsCount.text = infos[position]?.appointsCount?.toString() ?: ""

        headers[position].start?.also {
            holder.tvDate.text = dateFormat.format(it.time)
        } ?: holder.tvDate.setText(R.string.msg_unknown_date)

        val avatar = avatars[position]
        if(avatar == null)
            holder.imgvAvatar.setImageResource(R.drawable.img_default_avatar)
        else
            holder.imgvAvatar.setImageBitmap(avatar)

        holder.itemView.setOnClickListener {
            navigateSeminarDetail(position)
        }
        holder.itemView.setOnTouchListener { v, event ->
            swipeoutListener.targetHolder = holder
            swipeoutDetector.onTouchEvent(event)
        }
    }
}