package by.vadim_churun.ordered.speechman2.views

import android.app.*
import android.content.Context
import android.util.Log
import by.vadim_churun.ordered.speechman2.R
import java.util.Calendar


object DialogFactory
{
    private val LOGTAG = DialogFactory::class.java.simpleName

    private val suggestion = Calendar.getInstance()
    private var isDialogShowing = false

    /** Sets initial date and time for the next [pickDate] or [pickTime].
      * The given [Calendar]'s fields are copied into the underlying [Calendar] instance. **/
    fun setNextSuggestion(src: Calendar, offsetMins: Int = 0)
    {
        suggestion.set(src.get(Calendar.YEAR),
            src.get(Calendar.MONTH),
            src.get(Calendar.DAY_OF_MONTH) )
        suggestion.set(Calendar.HOUR_OF_DAY, src.get(Calendar.HOUR_OF_DAY))
        suggestion.set(Calendar.MINUTE, src.get(Calendar.MINUTE))
        suggestion.add(Calendar.MINUTE, offsetMins)

    }

    fun pickTime(context: Context, onTimeSet: (hour: Int, minute: Int) -> Unit)
    {
        if(isDialogShowing) return
        isDialogShowing = true

        TimePickerDialog(context,
            android.R.style.Theme_Holo_Light_Dialog,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                suggestion.set(Calendar.HOUR_OF_DAY, hourOfDay)
                suggestion.set(Calendar.MINUTE, minute)
                onTimeSet(hourOfDay, minute)
            },
            suggestion.get(Calendar.HOUR_OF_DAY),
            suggestion.get(Calendar.MINUTE),
            true
        ).apply {
            setOnDismissListener {
                Log.i(LOGTAG, "Dialog dismissed.")
                isDialogShowing = false
            }
            show()
        }
    }

    fun pickDate(context: Context, onDateSet: (day: Int, month: Int, year: Int) -> Unit)
    {
        if(isDialogShowing) return
        isDialogShowing = true

        DatePickerDialog(context,
            R.style.DateDialogDefaultTheme,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                suggestion.set(year, month, dayOfMonth)
                onDateSet(dayOfMonth, month, year)
            },
            suggestion.get(Calendar.YEAR),
            suggestion.get(Calendar.MONTH),
            suggestion.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener {
                Log.i(LOGTAG, "Dialog dismissed.")
                isDialogShowing = false
            }
            show()
        }
    }
}