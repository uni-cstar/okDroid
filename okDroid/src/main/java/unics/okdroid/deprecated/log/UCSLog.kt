package unics.okdroid.deprecated.util.log

import android.util.Log
import androidx.annotation.IntDef
import unics.okdroid.deprecated.log.UCSLazyLog

/**
 * @see unicstar.droid.core.util.LazyLog 建议kotlin使用该方法，可以避免某些字符串变量的内存分配
 */
interface UCSLog {

//    /**
//     * log是否可用
//     * @link https://blog.csdn.net/u013082948/article/details/80570123
//     */
//    fun isLoggable(tag: String, @LogLevel level: Int): Boolean {
//        return Log.isLoggable(tag, level)
//    }

    fun v(tag: String, message: String) {
        Log.v(tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    fun e(tag: String, message: String, e: Throwable) {
        Log.e(tag, message, e)
    }

    @IntDef(VERBOSE, DEBUG, INFO, WARN, ERROR)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LogLevel

    companion object DEFAULT : UCSLog {

        const val VERBOSE = 2

        /**
         * Priority constant for the println method; use Log.d.
         */
        const val DEBUG = 3

        /**
         * Priority constant for the println method; use Log.i.
         */
        const val INFO = 4

        /**
         * Priority constant for the println method; use Log.w.
         */
        const val WARN = 5

        /**
         * Priority constant for the println method; use Log.e.
         */
        const val ERROR = 6

//        /**
//         * Priority constant for the println method.
//         */
//        const val ASSERT = 7

        @JvmStatic
        fun lazy(tag: String): UCSLazyLog {
            return UCSLazyLog(tag)
        }
    }
}