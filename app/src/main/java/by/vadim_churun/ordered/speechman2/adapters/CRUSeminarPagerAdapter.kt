package by.vadim_churun.ordered.speechman2.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import by.vadim_churun.ordered.speechman2.R
import by.vadim_churun.ordered.speechman2.dests.sems.cru.*
import by.vadim_churun.ordered.speechman2.dests.CRUView


class CRUSeminarPagerAdapter(val context: Context): PagerAdapter()
{
    private val purposeResIDs = listOf(
        R.string.seminar_general_cru_purpose,
        R.string.seminar_image_cru_purpose,
        R.string.semdays_cru_purpose,
        R.string.semcosts_cru_purpose
    )

    private val pages = MutableList<CRUView?>(purposeResIDs.size) { null }


    fun getPagePurpose(position: Int): String
        = context.getString( purposeResIDs[position] )

    fun getComponentIdForPage(position: Int): Int
    {
        when(position)
        {
            0 -> return SeminarGeneralCRUPage.CRU_COMPONENT_ID
            1 -> return SeminarImageCRUPage.CRU_COMPONENT_ID
            2 -> return SemDaysCRUPage.CRU_COMPONENT_ID
            3 -> return SemCostsCRUPage.CRU_COMPONENT_ID
        }
        throw IllegalArgumentException("Adapter has only pages 0 to 3, no page $position")
    }

    fun getPageForComponentId(componentID: Int): Int
    {
        when(componentID)
        {
            SeminarGeneralCRUPage.CRU_COMPONENT_ID -> return 0
            SeminarImageCRUPage.CRU_COMPONENT_ID -> return 1
            SemDaysCRUPage.CRU_COMPONENT_ID -> return 2
            SemCostsCRUPage.CRU_COMPONENT_ID -> return 3
        }
        throw IllegalArgumentException("Invalid componentID $componentID")
    }

    fun getErrorSourcePosition(errorMessageResID: Int): Int
    {
        when(errorMessageResID)
        {
            R.string.msg_no_seminar_name -> return 0
            R.string.msg_no_seminar_city -> return 0
            else -> throw IllegalArgumentException("Unknown error message")
        }
    }


    override fun getCount(): Int
        = 4

    override fun isViewFromObject(view: View, obj: Any): Boolean
        = (view == obj)

    override fun instantiateItem(container: ViewGroup, position: Int): Any
    {
        var page = pages[position]

        if(page == null) {
            when(purposeResIDs[position])
            {
                R.string.seminar_general_cru_purpose -> {
                    page = SeminarGeneralCRUPage(context)
                }
                R.string.seminar_image_cru_purpose -> {
                    page = SeminarImageCRUPage(context)
                }
                R.string.semdays_cru_purpose -> {
                    page = SemDaysCRUPage(context)
                }
                R.string.semcosts_cru_purpose -> {
                    page = SemCostsCRUPage(context)
                }
            }
            pages[position] = page
        }

        container.addView(page!!)
        return page
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any)
        = container.removeView(obj as View)
}