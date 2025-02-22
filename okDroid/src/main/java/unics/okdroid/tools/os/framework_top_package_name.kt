@file:JvmName("FrameworksKt")
@file:JvmMultifileClass


package unics.okdroid.tools.os

import android.Manifest
import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.annotation.RequiresPermission
import unics.okdroid.deprecated.log.loge
import java.util.Calendar

interface TopPackageNameReader {
    fun get(context: Context): String?
}

/**
 * 只支持5.0及以下的兼容
 */
internal class TopPackageNameReaderDefault : TopPackageNameReader {

    @RequiresPermission(Manifest.permission.GET_TASKS)
    override fun get(context: Context): String? {
        try {
            val am =
                context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
                    ?: return null
            return am.runningAppProcesses.firstOrNull {
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }?.processName
        } catch (e: Throwable) {
            loge("TopPackageNameReader", e) {
                "[TopPackageNameReaderDefault] get top package name fail. $e"
            }
            return null
        }
    }

}

/**
 * 通过获取统计信息读取栈顶程序的包名
 * 该方式需要再清单中申明[Manifest.permission.PACKAGE_USAGE_STATS]
 * 并且在没有获取权限的情况下（使用[canUsageStats]方法进行判断），使用[requestPermissionIntent]发起权限请求
 */
@TargetApi(value = Build.VERSION_CODES.LOLLIPOP_MR1)
internal class TopPackageNameReaderApi22 : TopPackageNameReader {

    companion object {

        /**
         * 用于请求权限的Intent
         */
        @JvmStatic
        fun requestPermissionIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

        /**
         * 是否拥有权限
         */
        @JvmStatic
        fun canUsageStats(context: Context): Boolean {
            val appOps =
                context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            var mode = 0
            mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            return if (mode == AppOpsManager.MODE_DEFAULT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
            } else {
                mode == AppOpsManager.MODE_ALLOWED
            }
        }
    }

    @RequiresPermission(value = "android.permission.PACKAGE_USAGE_STATS")
    override fun get(context: Context): String? {
        try {
            val usageStatsManager =
                context.applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                    ?: return null

            val calendar = Calendar.getInstance()
            val end = calendar.timeInMillis
            val start = end - 5 * 60 * 1000 //请求30秒内的事件，避免数据太多
            val usageEvents = usageStatsManager.queryEvents(start, end)
            val event = UsageEvents.Event()
            var packageName: String? = null
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    packageName = event.packageName
                }
            }
            return packageName
        } catch (e: Throwable) {
            loge("TopPackageNameReader", e) {
                "[TopPackageNameReaderApi22] get top package name fail. $e"
            }
            return null
        }
    }

}