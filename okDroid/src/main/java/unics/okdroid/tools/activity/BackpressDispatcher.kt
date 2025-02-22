/**
 * Fragment返回键拦截
 */
@file:JvmName("ActivityKt")
@file:JvmMultifileClass

package unics.okdroid.tools.activity

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import unics.okdroid.deprecated.AutoLifecycle

/**
 * 添加返回键点击监听;自动绑定了生命周期
 * 必须在[Fragment.onAttach]之后调用，否则因为没有绑定Activity导致绑定失败
 */
@AutoLifecycle
fun Fragment.addOnBackPressed(
    lifecycleOwner: LifecycleOwner,
    onBackPressed: () -> Boolean
): OnBackPressedCallback {
    return requireActivity().addOnBackPressed(lifecycleOwner, onBackPressed)
}

/**
 * 添加返回键点击监听
 * 必须在[Fragment.onAttach]之后调用，否则因为没有绑定Activity导致绑定失败
 */
fun Fragment.addOnBackPressed(onBackPressed: () -> Boolean): OnBackPressedCallback {
    return requireActivity().addOnBackPressed(this, onBackPressed)
}

/**
 * 绑定返回键回调（建议使用该方法）
 * @param owner Receive callbacks to a new OnBackPressedCallback when the given LifecycleOwner is at least started.
 * This will automatically call addCallback(OnBackPressedCallback) and remove the callback as the lifecycle state changes. As a corollary, if your lifecycle is already at least started, calling this method will result in an immediate call to addCallback(OnBackPressedCallback).
 * When the LifecycleOwner is destroyed, it will automatically be removed from the list of callbacks. The only time you would need to manually call OnBackPressedCallback.remove() is if you'd like to remove the callback prior to destruction of the associated lifecycle.
 * @param onBackPressed 回调方法；返回true则表示消耗了按键事件，事件不会继续往下传递，相反返回false则表示没有消耗，事件继续往下传递
 * @return 注册的回调对象，如果想要移除注册的回调，直接通过调用[OnBackPressedCallback.remove]方法即可。
 */
@AutoLifecycle
fun androidx.activity.ComponentActivity.addOnBackPressed(
    owner: LifecycleOwner,
    onBackPressed: () -> Boolean
): OnBackPressedCallback {
    return backPressedCallback(onBackPressed).also {
        onBackPressedDispatcher.addCallback(owner, it)
    }
}

/**
 * 绑定返回键回调，未关联生命周期，建议使用关联生命周期的办法（尤其在fragment中使用，应该关联fragment的生命周期）
 */
fun androidx.activity.ComponentActivity.addOnBackPressed(onBackPressed: () -> Boolean): OnBackPressedCallback {
    return backPressedCallback(onBackPressed).also {
        onBackPressedDispatcher.addCallback(it)
    }
}

private fun androidx.activity.ComponentActivity.backPressedCallback(onBackPressed: () -> Boolean): OnBackPressedCallback {
    return object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!onBackPressed()) {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }
}
