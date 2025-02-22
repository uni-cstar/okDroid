/**
 * @author: chaoluo10
 * @date: 2022/4/27
 * @desc:
 */

//不能修改包名，否则无法访问包可见的内容:ViewPager.LayoutParams.position
package androidx.viewpager.widget

import android.view.View

/**
 * 获取ViewPager当前视图
 */
fun ViewPager.getCurrentView(): View? {
    val currentItem = currentItem
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        val layoutParams = child.layoutParams as? ViewPager.LayoutParams ?: continue
        if (!layoutParams.isDecor && currentItem == layoutParams.position) {
            return child
        }
    }
    return null
}