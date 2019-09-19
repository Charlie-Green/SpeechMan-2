package by.vadim_churun.ordered.speechman2.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText


class TouchableEditText: EditText
{
    constructor(context: Context):
        super(context)
    constructor(context: Context, attrs: AttributeSet?):
        super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
        super(context, attrs, defStyleAttr)


    private var onTouch: OnTouchListener? = null

    override fun setOnTouchListener(onTouchListener: OnTouchListener?)
    { this.onTouch = onTouchListener }

    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        onTouch?.onTouch(this, event)
        return super.onTouchEvent(event)
    }
}