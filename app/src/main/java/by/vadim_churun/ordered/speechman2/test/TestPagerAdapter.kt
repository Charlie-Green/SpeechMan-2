package by.vadim_churun.ordered.speechman2.test

import android.content.Context
import androidx.fragment.app.*
import by.vadim_churun.ordered.speechman2.model.objects.RemoteData


class TestPagerAdapter(
    val context: Context,
    fragmMan: FragmentManager,
    val data: RemoteData
): FragmentStatePagerAdapter(
    fragmMan, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT )
{
    override fun getCount(): Int
        = 3

    override fun getItem(position: Int): Fragment
    {
        TestRepository.data = data

        val fragm: TestFragment
        when(position)
        {
            0    -> fragm = RemoteEntitiesFragment()
            1    -> fragm = RemoteLacksFragment()
            else -> fragm = RemoteWarningsFragment()
        }
        return fragm
    }
}