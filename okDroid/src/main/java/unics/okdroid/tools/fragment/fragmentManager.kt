package unics.okdroid.tools.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * Created by Lucio on 2021/3/4.
 */

fun FragmentManager.switchFragment(
    fromFragmentTag: String,
    toFragmentTag: String,
    containerViewId: Int,
    toFragmentCreator: () -> Fragment
) {
    val fromFragment = findFragmentByTag(fromFragmentTag)
    switchFragment(fromFragment, toFragmentTag, containerViewId, toFragmentCreator)
}

fun FragmentManager.switchFragment(
    fromFragment: Fragment?,
    toFragmentTag: String,
    containerViewId: Int,
    toFragmentCreator: () -> Fragment
) {
    var toFragment = findFragmentByTag(toFragmentTag)

    if (toFragment == null) {
        toFragment = toFragmentCreator.invoke()
    }
    switchFragment(fromFragment, containerViewId, toFragmentTag, toFragment)
}


/**
 * @param fromFragment 当前fragment
 * @param toFragment 要切换的Fragment
 */
@JvmOverloads
fun FragmentManager.switchFragment(
    fromFragment: Fragment?,
    containerViewId: Int,
    toFragmentTag: String? = null,
    toFragment: Fragment
) {
    switchFragment(fromFragment, containerViewId, toFragmentTag, toFragment, false, null)
}

/**
 * @param fromFragment 当前fragment
 * @param toFragment 要切换的Fragment
 */
fun FragmentManager.switchFragment(
    fromFragment: Fragment?,
    containerViewId: Int,
    toFragmentTag: String?,
    toFragment: Fragment,
    addToBackStack: Boolean,
    backStackTag: String?
) {
    if (fromFragment == toFragment)
        return

    val ft = beginTransaction()

    if (!toFragment.isAdded) {
        ft.add(containerViewId, toFragment, toFragmentTag)
    } else {
        ft.show(toFragment)
    }

    fromFragment?.let {
        ft.hide(fromFragment)
    }

    if (addToBackStack) {
        ft.addToBackStack(backStackTag)
    }
    ft.commit()
}

fun FragmentManager.showFragment(tag: String, containerId: Int, creator: () -> Fragment) {
    showFragment(tag, containerId, false, creator)
}

fun FragmentManager.showFragment(
    tag: String,
    containerId: Int,
    addToBackStack: Boolean,
    creator: () -> Fragment
) {
    val cacheFragment = findFragmentByTag(tag)
    if (cacheFragment != null) {
        return
    }
    val ft = beginTransaction()
    ft.add(containerId, creator(), tag)
    if(addToBackStack){
        ft.addToBackStack(tag)
    }
    ft.commitNowAllowingStateLoss()
}

fun FragmentManager.removeFragment(tag: String) {
    val fragment = findFragmentByTag(tag) ?: return
    beginTransaction().remove(fragment).commit()
}

fun FragmentManager.removeFragmentFromBackStack(tag: String) {
    val fragment = findFragmentByTag(tag) ?: return
    popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
}

fun FragmentActivity.showFragment(tag: String, containerId: Int, creator: () -> Fragment) {
    supportFragmentManager.showFragment(tag, containerId, creator)
}

fun FragmentActivity.removeFragment(tag: String) {
    supportFragmentManager.removeFragment(tag)
}

fun Fragment.showFragment(tag: String, containerId: Int, creator: () -> Fragment) {
    childFragmentManager.showFragment(tag, containerId, creator)
}

fun Fragment.removeFragment(tag: String) {
    childFragmentManager.removeFragment(tag)
}