package com.enviroclean.customeview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.base.BaseBindingAdapter

/**
 * Created by imobdev-paresh on 10,December,2019
 */
abstract class PaginationScrollListenerLinear(private var layoutManager: LinearLayoutManager,
                                              val isForGrid: Boolean = false) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        /*val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (!isLoading() && !isLastPage()) {
            if (visibleItemCount + firstVisibleItemPosition + 1 >= totalItemCount
                && firstVisibleItemPosition >= 0
                && totalItemCount + 1 >= getTotalPageCount()
            ) {
                loadMoreItems()
            }
        }*/

        if(isForGrid) {
            /*if (!recyclerView.canScrollVertically(1)) {
                if (!isLoading() && !isLastPage()) {
                    loadMoreItems()
                }
            }*/
            val totalItemCount = layoutManager.itemCount
            val lastVisible = layoutManager.findLastVisibleItemPosition()

            val endHasBeenReached = lastVisible + 1 >= totalItemCount
            if (totalItemCount > 0 && endHasBeenReached) {
                if (!isLoading() && !isLastPage()) {
                    loadMoreItems()
                }
            }
        } else {
            val dataSize = (recyclerView.adapter as BaseBindingAdapter<*>).itemCount
            if (layoutManager.findLastCompletelyVisibleItemPosition() == dataSize - 1) {
                if (!isLoading() && !isLastPage()) {
                    loadMoreItems()
                }
            }
        }
    }

    /*override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
    }*/

    protected abstract fun loadMoreItems()

    abstract fun getTotalPageCount(): Int

    abstract fun isLastPage(): Boolean

    abstract fun isLoading(): Boolean
}