package by.vadim_churun.ordered.speechman2.views

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText


class SingleWatcherEditText: EditText
{
    constructor(context: Context):
        super(context)
    constructor(context: Context, attrs: AttributeSet?):
        super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
        super(context, attrs, defStyleAttr)


    private var textWatcher: TextWatcher? = null

    override fun addTextChangedListener(watcher: TextWatcher?)
    {
        textWatcher?.also { super.removeTextChangedListener(it) }
        textWatcher = watcher
        super.addTextChangedListener(watcher)
    }
}