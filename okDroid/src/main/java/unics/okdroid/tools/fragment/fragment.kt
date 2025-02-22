package unics.okdroid.tools.fragment

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import unics.okdroid.tools.os.checkValidationOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

inline fun Fragment.launchBlockRepeatOnLifecycle(
    state: Lifecycle.State,
    noinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(state, block)
    }
}

/**
 * 快速运行一个Activity
 */
fun Fragment.startActivity(clazz: Class<out Activity>) {
    val it = Intent(this.requireContext(), clazz)
    startActivity(it)
}

fun Fragment.startActivitySafely(intent: Intent): Boolean {
    return try {
        intent.checkValidationOrThrow(this.requireContext())
        this.startActivity(intent)
        true
    } catch (e: Throwable) {
        Log.w(this::class.java.simpleName, "无法打开指定Intent", e)
        false
    }
}

fun Fragment.startActivityForResultSafely(intent: Intent, requestCode: Int): Boolean {
    return try {
        intent.checkValidationOrThrow(this.requireContext())
        this.startActivityForResult(intent, requestCode)
        true
    } catch (e: Throwable) {
        Log.w(this::class.java.simpleName, "无法打开指定Intent", e)
        false
    }
}

