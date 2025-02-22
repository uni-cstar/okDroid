package unics.okdroid.kit.debugger

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.coroutineScope
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime
import kotlin.time.toDuration

/**
 * Create by luochao
 * on 2023/12/14
 *
 * 测量耗时:该场景用于连续测量时间，单次测量请使用官方提供的类
 * @see measureTime
 * @see TimeSource.Monotonic.markNow
 */
interface TimeMeasure {

    /**
     * 重置
     */
    fun reset()

    /**
     * 标记现在
     */
    fun markNow(context: Context): Long

    /**
     * 打印[markNow]现在时间
     */
    fun printNow(subTag: String = ""): String

    /**
     * 打印消息
     */
    fun printMessage(message:String)

    /**
     * 打印方法耗时时间
     */
    fun <R> printMeasureTime(tag: String, block: () -> R): R

    /**
     * 打印方法耗时时间
     */
    suspend fun <R> printSuspendMeasureTime(
        tag: String,
        block: suspend () -> R
    ): R

    companion object {

        @JvmStatic
        fun create(
            tag: String = "TimeMeasure",
            handler: MessageHandler = MessageHandler.Printer
        ): TimeMeasure {
            return Normal(tag, handler)
        }

        @JvmStatic
        fun lazy(tag: String = "TimeMeasure"): Lazy {
            return Lazy(tag)
        }
    }

    /**
     * 普通处理方式：直接打印信息
     */
    open class Normal internal constructor(
        private val tag: String,
        private val handler: MessageHandler
    ) : TimeMeasure {

        private var start = 0L

        private var now = 0L

        init {
            reset()
        }

        /**
         * 重置使用
         */
        final override fun reset() {
            start = SystemClock.elapsedRealtime()
            now = start
        }

        /**
         * 标记现在
         */
        override fun markNow(context: Context): Long {
            now = SystemClock.elapsedRealtime()
            return start
        }

        /**
         * 打印[markNow]现在时间
         */
        override fun printNow(subTag: String): String {
            val time = SystemClock.elapsedRealtime()
            val msg =
                "$subTag: after last elapse,cost time ${(time - now).toDuration(DurationUnit.MILLISECONDS)}, total ${
                    (time - start).toDuration(DurationUnit.MILLISECONDS)
                }"
            now = time
            handler.handle(tag, msg)
            return msg
        }

        override fun printMessage(message: String) {
            handler.handle("",message)
        }

        /**
         * 打印方法耗时时间
         */
        @OptIn(ExperimentalTime::class)
        override fun <R> printMeasureTime(tag: String, block: () -> R): R {
            val result: R
            val time = measureTime {
                result = block.invoke()
            }
            handler.handle(tag, "method measure time $time")
            return result
        }

        @OptIn(ExperimentalTime::class)
        override suspend fun <R> printSuspendMeasureTime(
            tag: String,
            block: suspend () -> R
        ): R {
            val result: R
            coroutineScope {
                val time = measureTime {
                    result = block.invoke()
                }
                handler.handle(tag, "method measure time $time")
            }
            return result
        }
    }

    class Lazy private constructor(
        tag: String,
        private val handler: MessageHandler.Lazy = MessageHandler.Lazy()
    ) : Normal(tag, handler) {

        constructor(tag: String) : this(tag, MessageHandler.Lazy())

        fun list(): List<String> {
            return handler.list()
        }

        fun clear() {
            handler.clear()
        }
    }

}

/**
 * 打印方法耗时时间
 */
@OptIn(ExperimentalTime::class)
inline fun <R> printSuspendMeasureTime(tag: String, block: () -> R): R {
    val result: R
    val time = measureTime {
        result = block.invoke()
    }
    MessageHandler.Printer.handle(tag, "method measure time ${time}ms")
    return result
}