//不能修改包路径，否则无法重写findScrollingChild方法
package com.google.android.material.bottomsheet

import android.content.Context
import android.util.AttributeSet
import android.view.View
import unics.okdroid.widget.findScrollingChildCompat

/**
 * @author: chaoluo10
 * @date: 2022/4/27
 * @desc: 用于修正嵌套滚动child view的查找策略:
 * 1、可滚动的view必须visible才能作为查找结果（官方内部只判断了ViewCompat.isNestedScrollingEnabled(view)）
 * 2、支持ViewPager：ViewPager只会使用当前页面的视图去查找
 * 3、支持完全的自定义查找规则
 */
open class BottomSheetBehaviorCompat<V : View> : BottomSheetBehavior<V> {

    interface ScrollingChildFinder {
        /**
         * 查找可滚动的childView，注意该方法会在layout期间多次回调（CoordinateLayout的每一个child view均会回调），避免做耗时操作
         * @param view 被查找的view
         */
        fun findScrollingChild(view: View): View?

    }

    private var mCustomScrollingChildFinder: ScrollingChildFinder? = null

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    override fun findScrollingChild(view: View): View? {
        if (mCustomScrollingChildFinder != null) {
            return mCustomScrollingChildFinder?.findScrollingChild(view)
        }
        return view.findScrollingChildCompat()
    }

}