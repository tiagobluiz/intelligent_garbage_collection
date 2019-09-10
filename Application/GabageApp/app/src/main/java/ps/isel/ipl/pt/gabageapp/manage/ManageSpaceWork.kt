package ps.isel.ipl.pt.gabageapp.manage

import android.app.LoaderManager
import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager

import kotlinx.android.synthetic.main.activity_manage_space_work.*
import ps.isel.ipl.pt.gabageapp.R
import ps.isel.ipl.pt.gabageapp.manage.fragments.ManageFragment
import ps.isel.ipl.pt.gabageapp.manage.fragments.ProfileFragment
import ps.isel.ipl.pt.gabageapp.manage.fragments.WorkSpaceFragment
import ps.isel.ipl.pt.gabageapp.util.loader.json_home.HomeJson

class ManageSpaceWork : AppCompatActivity(){

    companion object {
        public val GET_HOME = "gethome"
    }
    private lateinit var home :HomeJson

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_space_work)

        home = intent.extras.get(GET_HOME)as HomeJson

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mViewPager = container
        mViewPager?.adapter = mSectionsPagerAdapter

        tabs.setupWithViewPager(mViewPager)
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return when (position) {
                0 -> ManageFragment(home.resources)
                1 -> WorkSpaceFragment(home.resources)
                2 -> ProfileFragment(home.resources)
                else -> WorkSpaceFragment(home.resources)
            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }




        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> getString(R.string.manage_fragment)
                1 -> getString(R.string.work_space_fragment)
                2 -> getString(R.string.profile_fragment)
                else -> getString(R.string.work_space_fragment)
            }

        }
    }

}
