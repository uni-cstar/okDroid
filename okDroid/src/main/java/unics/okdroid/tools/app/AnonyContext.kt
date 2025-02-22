package unics.okdroid.tools.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment

/**
 * Created by Lucio on 2019/7/21.
 * 匿名上下文
 */
interface AnonyContext {

    val realCtx: Context

    fun startActivity(intent: Intent)

    fun startActivityForResult(intent: Intent, requestCode: Int)

    fun finish()
//
//    fun isPermissionGranted(permission: String): Boolean {
//        return ContextCompat.checkSelfPermission(
//            realCtx,
//            permission
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    fun requestPermission(permissions: Array<String>, requestCode: Int)

    private class ActivityContext(val activity: Activity) : AnonyContext {
        override val realCtx: Context
            get() = activity

        override fun startActivity(intent: Intent) {
            activity.startActivity(intent)
        }

        override fun startActivityForResult(intent: Intent, requestCode: Int) {
            activity.startActivityForResult(intent, requestCode)
        }

        override fun finish() {
            activity.finish()
        }

//        override fun requestPermission(permissions: Array<String>, requestCode: Int) {
//            ActivityCompat.requestPermissions(activity, permissions, requestCode)
//        }
    }

    private class JustContext(val ctx: Context) : AnonyContext {
        override val realCtx: Context
            get() = ctx

        override fun startActivity(intent: Intent) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }

        override fun startActivityForResult(intent: Intent, requestCode: Int) {
            startActivity(intent)
        }

        override fun finish() {
            Log.w("AnonyContext","在Context上执行finish没有任何效果")
        }

//        override fun requestPermission(permissions: Array<String>, requestCode: Int) {
//            TODO("Context不支持该操作")
//        }
    }

    private class FragmentContext(val fragment: Fragment) : AnonyContext {
        override val realCtx: Context
            get() = fragment.requireContext()

        override fun startActivity(intent: Intent) {
            fragment.startActivity(intent)
        }

        override fun startActivityForResult(intent: Intent, requestCode: Int) {
            fragment.startActivityForResult(intent, requestCode)
        }

        override fun finish() {
            fragment.activity?.finish()
        }

//        override fun requestPermission(permissions: Array<String>, requestCode: Int) {
//            fragment.requestPermissions(permissions, requestCode)
//        }
    }

    companion object {

        fun new(ctx: Context): AnonyContext {
            if (ctx is AnonyContext)
                return ctx
            return if (ctx is Activity) {
                ActivityContext(ctx)
            } else {
                JustContext(ctx)
            }
        }

        fun new(fragment: Fragment): AnonyContext {
            if (fragment is AnonyContext)
                return fragment
            return (FragmentContext(fragment))
        }

    }

}