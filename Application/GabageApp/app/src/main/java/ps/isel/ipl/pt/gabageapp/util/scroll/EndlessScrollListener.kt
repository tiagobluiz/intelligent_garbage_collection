package ps.isel.ipl.pt.gabageapp.util.scroll

import android.view.View
import android.widget.AbsListView

/**
 * Created by goncalo on 26/05/2018.
 */
abstract class EndlessScrollListener : AbsListView.OnScrollListener, View.OnScrollChangeListener {
    override fun onScrollChange(v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // The minimum number of items to have below your current scroll position
    // before loading more.
    private var visibleThreshold = 5
    // The current offset index of data you have loaded
    private var currentPage = 0
    // The total number of items in the dataset after the last load
    private var previousTotalItemCount = 0
    // True if we are still waiting for the last set of data to load.
    private var loading = true
    // Sets the starting page index
    private var startingPageIndex = 0
    // Current Link
    private lateinit var currentLink: String
    // Next Link
    private lateinit var nextLink: String

    constructor(visibleThreshold: Int) {
        this.visibleThreshold = visibleThreshold
        nextLink = ""
        currentLink = nextLink
    }


    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex
            this.previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) {
                this.loading = true
            }
        }

        // If it's still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
            currentPage++
        }

        // If it isn't currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if (!loading && firstVisibleItem + visibleItemCount + visibleThreshold > totalItemCount && !nextLink.equals("")){
            //loading = onLoadMore(currentPage + 1, totalItemCount)
            currentPage++
            loading = onLoadMore(nextLink)
        }
    }

    fun setNewLink(newLink: String){
        this.currentLink = this.nextLink
        this.nextLink = newLink
    }

    // Defines the process for actually loading more data based on page
    // Returns true if more data is being loaded; returns false if there is no more data to load.
    abstract fun onLoadMore(link: String ): Boolean


    override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
        //Nada
    }
}