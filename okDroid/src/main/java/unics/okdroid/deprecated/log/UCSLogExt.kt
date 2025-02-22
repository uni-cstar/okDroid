package unics.okdroid.deprecated.log

import unics.okdroid.deprecated.LogLevel
import unics.okdroid.deprecated.util.log.UCSLog

/**
 * 默认的Log
 */
var LOGGER: UCSLog = UCSLog

/**
 * 日志等级
 */
@LogLevel
var logLevel: Int = UCSLog.DEBUG

/**
 * 日志是否可用
 */
inline fun isLoggable(@UCSLog.LogLevel level: Int): Boolean {
    return level >= logLevel
}

/**
 * 内联 -> 消息懒加载
 */
inline fun logv(tag: String, creator: () -> String) {
    if (isLoggable(UCSLog.VERBOSE)) {
        LOGGER.v(tag, creator())
    }
}

/**
 * 内联 -> 消息懒加载
 */
inline fun logd(tag: String, creator: () -> String) {
    if (isLoggable(UCSLog.DEBUG)) {
        LOGGER.d(tag, creator())
    }
}

/**
 * 内联 -> 消息懒加载
 */
inline fun logi(tag: String, creator: () -> String) {
    if (isLoggable(UCSLog.INFO)) {
        LOGGER.i(tag, creator())
    }
}

/**
 * 内联 -> 消息懒加载
 */
inline fun loge(tag: String, creator: () -> String) {
    if (isLoggable(UCSLog.ERROR)) {
        LOGGER.e(tag, creator())
    }
}

/**
 * 内联 -> 消息懒加载
 */
inline fun loge(tag: String, e: Throwable, creator: () -> String) {
    if (isLoggable(UCSLog.ERROR)) {
        LOGGER.e(tag, creator(), e)
    }
}

/*以下为内部库使用*/
internal val ILOGGER = UCSLog.lazy("UCSCore")

internal inline fun ilogv(creator: () -> String) {
    ILOGGER.v(creator)
}

internal inline fun ilogd(creator: () -> String) {
    ILOGGER.d(creator)
}

internal inline fun ilogi(creator: () -> String) {
    ILOGGER.i(creator)
}

internal inline fun loge(creator: () -> String) {
    ILOGGER.e(creator)
}

internal inline fun loge(creator: () -> String, e: Throwable) {
    ILOGGER.e(creator)
}


