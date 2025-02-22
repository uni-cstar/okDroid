/**
 * Created by Lucio on 2021/12/1.
 */
@file:JvmName("ActivityKt")
@file:JvmMultifileClass
package unics.okdroid.tools.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import unics.okdroid.tools.os.checkValidationOrThrow
import unics.okcore.exception.onCatch
import unics.okcore.exception.tryIgnore
import kotlin.reflect.KClass

/**
 * 设置Activity是否响应触摸；enable=true 响应。
 */
fun Activity.enableTouchable(enable: Boolean) {
    if (enable) {
        //去掉不响应触摸标记
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    } else {
        //设置不可触摸标记
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}

/**
 * 从上下文中获取[Activity]
 */
val Context.activityContext: Activity?
    get() {
        var context: Context? = this
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

///**
// * 递归方式：从上下文中获取[Activity]
// */
//fun Context.getActivityContext(): Activity? {
//    if (this is Activity) {
//        return this
//    } else if (this is ContextWrapper) {
//        return this.baseContext.getActivityContext()
//    }
//    return null
//}


/**
 * 快速运行一个Activity
 */
fun Context.startActivity(clazz: KClass<out Activity>) {
    val it = Intent(this, clazz.java)
    if (this !is Activity)
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(it)
}

fun Activity.startActivity(clazz: KClass<out Activity>) {
    val it = Intent(this, clazz.java)
    startActivity(it)
}

fun Context.startActivitySafely(intent: Intent) {
    tryIgnore {
        intent.checkValidationOrThrow(this)
        if (this !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        this.startActivity(intent)
    }.onCatch {
        Log.w(this::class.java.simpleName, "无法打开指定Intent", it)
    }
}

fun Activity.startActivityForResultSafely(intent: Intent, requestCode: Int) :Boolean{
    return try {
        intent.checkValidationOrThrow(this)
        this.startActivityForResult(intent, requestCode)
        true
    }catch (e:Throwable) {
        Log.w(this::class.java.simpleName, "无法打开指定Intent", e)
        false
    }
}

/**
 * content view结点
 */
val Activity.contentView: ViewGroup
    get() = this.findViewById(android.R.id.content)
//        ?: findViewById(R.id.content)