package unics.okdroid.deprecated

import unics.okdroid.deprecated.util.log.UCSLog


/**
 * 默认的Log
 */
inline var LOGGER: UCSLog
    get() = unics.okdroid.deprecated.log.LOGGER
    set(value) {
        unics.okdroid.deprecated.log.LOGGER = value
    }

/**
 * 日志等级
 */
inline var logLevel: Int
    @LogLevel
    get() = unics.okdroid.deprecated.log.logLevel
    set(@LogLevel value) {
        unics.okdroid.deprecated.log.logLevel = value
    }