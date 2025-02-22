package unics.okdroidarch.support.multidevice

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.SimpleArrayMap
import com.google.android.material.theme.MaterialComponentsViewInflater
import java.lang.reflect.Constructor

/**
 * 在TV上使用的控件，在Touch设备上的兼容使用：主要是将[View.isFocusableInTouchMode]设置为true的，在touch设备上设置为false
 * @param disableFocusTouchMode 开启之后会禁用[View.isFocusableInTouchMode]修改功能
 * @param excludesViewClass 排除不做处理的view类型。比如[EditText]无论在电视还是手机上本身就是[EditText.isFocusableInTouchMode]=true的运行模式，因此内部默认添加了[EditText]的排除
 */
class LeanbackTouchCompatViewInflater(
    private val activity: AppCompatActivity,
    private val disableFocusTouchMode: Boolean = true,
    excludesViewClass: List<Class<out View>>? = null
) :
    LayoutInflater.Factory2 {

    private val mConstructorArgs = arrayOfNulls<Any?>(2)

    /**
     * 排除的类型
     */
    private val mExcludesViewTypes = mutableListOf<Class<out View>>(
        EditText::class.java
    )

    private var mCustomExcludeFilter: CustomExcludeViewFilter? = null

    /**
     * 添加排除的类型
     */
    fun addExcludeViewClass(clazz: Class<out View>) {
        mExcludesViewTypes.add(clazz)
    }

    /**
     * 移除排除的view类型
     */
    fun removeExcludeViewClass(clazz: Class<*>) {
        mExcludesViewTypes.remove(clazz)
    }

    fun setCustomExcludeFilter(filter: CustomExcludeViewFilter?) {
        mCustomExcludeFilter = filter
    }

    init {
        if (!excludesViewClass.isNullOrEmpty()) {
            mExcludesViewTypes.addAll(excludesViewClass)
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        //没有禁用focus模式，相当于什么都没做
        if (!disableFocusTouchMode)
            return null

        var view = activity.delegate.createView(parent, name, context, attrs)
        if (view == null) {
            Log.d("MultiDeviceViewFactory", "activity.delegate return null.")
            view = createViewFromTag(context, name, attrs)
            if (view == null) {
                Log.d("MultiDeviceViewFactory", "createViewFromTag return null.")
            } else {
                Log.d("MultiDeviceViewFactory", "createViewFromTag return  $view")
            }
        } else {
            Log.d("MultiDeviceViewFactory", "activity.delegate return $view")
        }
        if (view != null) {
            handleView(view)
        }
        return view
    }

    private fun handleView(view: View) {
        handleFocusInTouchMode(view)
    }

    private fun handleFocusInTouchMode(view: View) {
        Log.d("MultiDeviceViewFactory", "handleFocusInTouchMode $view")
        if (!view.isFocusableInTouchMode) {
            Log.d(
                "MultiDeviceViewFactory",
                "[handleFocusInTouchMode] isFocusableInTouchMode = false,不用额外处理 $view"
            )
            return
        }

        val isExclude = mExcludesViewTypes.any { excludeClass ->
            excludeClass.isAssignableFrom(view.javaClass)
        }
        if (isExclude) {
            Log.d("MultiDeviceViewFactory", "[handleFocusInTouchMode] $view 被排除，不进行处理")
            return
        }

        if (mCustomExcludeFilter?.invoke(view) == true) {
            Log.d("MultiDeviceViewFactory", "[handleFocusInTouchMode] $view 被自定义规则排除，不进行处理")
            return
        }


        view.isFocusableInTouchMode = false
        Log.d(
            "MultiDeviceViewFactory",
            "[handleFocusInTouchMode] $view isFocusableInTouchMode 修改为false"
        )
    }

    private fun createViewFromTag(context: Context, name: String, attrs: AttributeSet): View? {
        var viewName = name
        if (viewName == "view") {
            viewName = attrs.getAttributeValue(null, "class")
        }
        return try {
            mConstructorArgs[0] = context
            mConstructorArgs[1] = attrs
            if (-1 == viewName.indexOf('.')) {
                for (i in sClassPrefixList.indices) {
                    val view = createViewByPrefix(
                        context, viewName,
                        sClassPrefixList[i]
                    )
                    if (view != null) {
                        return view
                    }
                }
                null
            } else {
                createViewByPrefix(context, viewName, null)
            }
        } catch (e: Exception) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater
            // try
            null
        } finally {
            // Don't retain references on context.
            mConstructorArgs[0] = null
            mConstructorArgs[1] = null
        }
    }


    @Throws(ClassNotFoundException::class, InflateException::class)
    private fun createViewByPrefix(context: Context, name: String, prefix: String?): View? {
        var constructor = sConstructorMap[name]
        return try {
            if (constructor == null) {
                // Class not found in the cache, see if it's real, and try to add it
                val clazz = Class.forName(
                    if (prefix != null) prefix + name else name,
                    false,
                    context.classLoader
                ).asSubclass(View::class.java)
                constructor = clazz.getConstructor(*sConstructorSignature)
                sConstructorMap.put(name, constructor)
            }
            constructor!!.isAccessible = true
            constructor.newInstance(*mConstructorArgs)
        } catch (e: java.lang.Exception) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater
            // try
            null
        }
    }

    companion object {
        private val sClassPrefixList: Array<String> = arrayOf<String>(
            "android.widget.",
            "android.view.",
            "android.webkit."
        )

        private val sConstructorMap = SimpleArrayMap<String, Constructor<out View?>>()

        private val sConstructorSignature = arrayOf(
            Context::class.java, AttributeSet::class.java
        )
    }
}

/**
 * 自定义排除规则
 * @return 返回true，则排除
 */
typealias CustomExcludeViewFilter = (View) -> Boolean