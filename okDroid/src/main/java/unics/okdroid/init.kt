package unics.okdroid

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.util.Base64
import unics.okdroid.tools.ui.droidExceptionHandler
import unics.okdroid.util.Logger
import unics.okdroid.tools.app.getMetaData
import unics.okcore.base64Decoder
import unics.okcore.base64Encoder
import unics.okcore.exceptionHandler
import unics.okcore.lang.security.md5
import unics.okcore.urlCoder
import unics.okdroid.kit.floatwindow.NoticeFloatWindowTools
import unics.okdroid.tools.ui.UI_MODE_TYPE_UNDEFINED
import unics.okdroid.tools.ui.currentUiModeType
import unics.okdroid.tools.app.internal.ApplicationManagerImpl
import unics.okdroid.tools.net.DroidURLCoder

/**
 * 全局的公用SharedPreferences
 */
val globalSharedPref: SharedPreferences by lazy {
    globalContext.applicationContext.getSharedPreferences(
        globalSharedPrefFileName(),
        Context.MODE_PRIVATE
    )
}

fun initOkDroid(
    app: Application,
    debuggable: Boolean = (app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
) {
    unics.okcore.debuggable = debuggable
    if (isInit) {
        return
    }
    isInit = true
    globalContext = app

    currentUiModeType = app.getMetaData(app.getString(R.string.bas_ui_mode), UI_MODE_TYPE_UNDEFINED)
    Logger.d("DroidCore", "currentUiModeType=$currentUiModeType")

    //设置URL编码
    urlCoder = DroidURLCoder
    //设置base64编解码
    val base64Coder = unics.okdroid.util.Base64(Base64.NO_WRAP)
    base64Encoder = base64Coder
    base64Decoder = base64Coder
    //设置异常处理器
    exceptionHandler = droidExceptionHandler
    //初始化应用管理器
    ApplicationManagerImpl.init(app)
    //优化悬浮窗管理
    NoticeFloatWindowTools.getInstance().init(app)
}

/**
 * 通过反射获取Application
 *
 */
@SuppressLint("PrivateApi")
@Deprecated("不推荐使用，内部api，可能会变更")
fun getApplication(): Application? {
    try {
        return Class.forName("android.app.ActivityThread")
            .getMethod("currentApplication").invoke(null) as Application
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    return null
}

@PublishedApi
internal lateinit var globalContext: Application
    private set

/**
 * 库是否已初始化
 */
internal var isInit: Boolean = false

internal fun globalSharedPrefFileName(): String {
    return "_${PREF_FILE_NAME.md5()}"
}

private const val PREF_FILE_NAME = "ucs_global_pref"


