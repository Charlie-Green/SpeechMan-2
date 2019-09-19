package by.vadim_churun.ordered.speechman2.views

import android.animation.*
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.RecyclerView


class SwipeOutListener: GestureDetector.SimpleOnGestureListener()
{
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // SETUP:

    interface OnSwipeOutCallback
    {
        fun onSwipeOut(vh: RecyclerView.ViewHolder)
    }

    lateinit var targetHolder: RecyclerView.ViewHolder
    lateinit var onSwipeOut: OnSwipeOutCallback


    //////////////////////////////////////////////////////////////////////////////////////////////////////

    private var initialX = 0f
    private var lastScrollTime = 0L
    private var deltaX = 0f

    override fun onDown(event: MotionEvent): Boolean
    {
        initialX = targetHolder.itemView.x
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean
    {
        targetHolder.itemView.performClick()
        return false
    }

    override fun onScroll
                (event1: MotionEvent, event2: MotionEvent, distanceX: Float, distanceY: Float): Boolean
    {
        if(distanceX >= 0f) return true
        deltaX += distanceX
        val now = System.currentTimeMillis()
        if(now - lastScrollTime >= 20L) {
            //Log.i(LOGTAG, "Distance: $distanceX")
            val wishedX = targetHolder.itemView.x - deltaX
            targetHolder.itemView.x = Math.max(wishedX, initialX)
            lastScrollTime = now
            deltaX = 0f
        }
        return true
    }

    fun onUp(): Boolean
    {
        // Save these values to local variable to make it immutable.
        val holder = targetHolder
        val target = holder.itemView
        val x0 = initialX

        // Log.i(LOGTAG, "Scroll up. translationX = ${target.translationX} out of ${target.width}")
        if(target.x - initialX < 0.25f*target.width) {
            // Scrolled to few. Restore the view's position.
            target.x = initialX
            return false
        }

        ObjectAnimator.ofFloat(
            target, "translationX", target.translationX, target.width.toFloat() ).apply {
            duration = 800L
            addListener( object: Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {   }
                override fun onAnimationCancel(animation: Animator) {   }
                override fun onAnimationRepeat(animation: Animator?) {   }

                override fun onAnimationEnd(animation: Animator)
                {
                    target.x = x0
                    onSwipeOut.onSwipeOut(holder)
                }
            } )
            start()
        }

        return false
    }
}