package unics.okdroid.tools.content

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes

/**
 * 用于无差别处理view和Activity find的逻辑
 */
interface FindViewProxy{

    val source:Any

    fun <T : View> findViewById(@IdRes id: Int): T?

    companion object {

        @JvmStatic
        fun create(activity: Activity): FindViewProxy {
            return object : FindViewProxy {
                override val source: Any
                    get() = activity

                override fun <T : View> findViewById(id: Int): T? {
                    return activity.findViewById(id)
                }
            }
        }

        @JvmStatic
        fun create(view: View): FindViewProxy {
            return object : FindViewProxy {
                override val source: Any
                    get() = view

                override fun <T : View> findViewById(id: Int): T? {
                    return view.findViewById(id)
                }
            }
        }

        inline fun Activity.asFindViewProxy(): FindViewProxy {
            return create(this)
        }

        inline fun View.asFindViewProxy(): FindViewProxy {
            return create(this)
        }
    }
}