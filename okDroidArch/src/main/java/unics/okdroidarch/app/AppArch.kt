package unics.okdroidarch.app


import android.app.Activity
import android.app.Application
import unics.okdroid.deprecated.log.logd
import unics.okdroid.kit.imageloader.Configs
import unics.okdroid.kit.imageloader.DEFAULT_DISK_CACHE_FOLDER_NAME
import unics.okdroid.kit.imageloader.initImageLoader
import unics.okdroidarch.initOkDroidArch
import unics.okdroid.tools.app.AppManager
import unics.okdroid.tools.app.ApplicationManager
import unics.okdroid.tools.os.processName
import unics.okdroid.util.Logger

/**
 * Created by Lucio on 2020-11-01.
 */
abstract class AppArch : Application(), ApplicationManager by AppManager {

    final override fun onCreate() {
        super.onCreate()
        setupBasDependencies()
        val processName = this.processName
        val packageName = this.packageName
        logd("AppArch") {
            "processName = $processName  packageName=$packageName"
        }
        if (processName == this.packageName) {
            logd("AppArch") { "invoke onCreateMainProcess" }
            onCreateMainProcess(processName)
        } else {
            logd("AppArch") { "invoke onCreateOtherProcess" }
            onCreateOtherProcess(processName)
        }
    }

    /**
     * 主进程执行[Application.onCreate]
     */
    protected open fun onCreateMainProcess(processName: String) {
        setupImageLoader()
    }

    /**
     * 子进程执行[Application.onCreate]
     */
    protected abstract fun onCreateOtherProcess(processName: String)

    /**
     * 初始化Bas依赖库
     */
    protected open fun setupBasDependencies() {
        initOkDroidArch(this)
    }

    /**
     * 初始化ImageLoader
     */
    protected open fun setupImageLoader() {
        initImageLoader(
            this, configs = Configs.Builder()
                .setDiskCacheEnabled(true)
                .setDiskCacheFolderName(DEFAULT_DISK_CACHE_FOLDER_NAME)
                .setMemoryCacheEnabled(true)
                .build()
        )
    }

    fun finishAllActivity() {
        activityStack.finishAll()
    }

    val topActivity: Activity? get() = activityStack.getCurrent()
}