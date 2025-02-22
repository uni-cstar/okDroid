package unics.okdroid.tools.content

import android.os.Build
import unics.okcore.debuggable
import unics.okdroid.tools.content.IDManager.Generator
import unics.okdroid.globalSharedPref
import java.util.*

object IDManager {

    private const val DEVICE_UNIQUE_ID = "_device_unique_id"

    fun interface Generator<T> {
        fun generate(): T

        companion object {

            /**
             * 默认唯一设备id生成器（自定义规则而已）
             */
            @JvmStatic
            fun deviceIDGenerator(): Generator<String> {
                return Generator {
                    val sb: StringBuilder = StringBuilder()
                    sb.append(Build.BRAND)//品牌
                        .append("_")
                        .append(Build.MODEL)//型号
                        .append("_")
                        .append(Build.VERSION.RELEASE)//设备系统版本号
                        .append("_")
                        .append(System.currentTimeMillis())//时间戳
                        .append(UUID.randomUUID().toString().replace("-", ""))
                    sb.toString()
                }
            }
        }
    }

    /**
     * 唯一设备id
     */
    val deviceUniqueID: String
        get() {
            if (debuggable) {
                return "super_luo_debug"
            }
            with(globalSharedPref) {
                var cache = getString(DEVICE_UNIQUE_ID, null)
                if (cache.isNullOrEmpty()) {
                    cache = Generator.deviceIDGenerator().generate()
                    edit {
                        putString(DEVICE_UNIQUE_ID, cache)
                    }
                }
                return cache
            }
        }
}
