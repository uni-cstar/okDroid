package unics.okdroid.deprecated.log

/**
 * 该类的目的只是为了避免频繁传Tag
 */
class UCSLazyLog(@JvmField @PublishedApi internal val tag: String) {

    /**
     * 内联 -> 消息懒加载
     */
    inline fun v(creator: () -> String) {
        logv(tag, creator)
    }

    /**
     * 内联 -> 消息懒加载
     */
    inline fun d(creator: () -> String) {
        logd(tag, creator)
    }

    /**
     * 内联 -> 消息懒加载
     */
    inline fun i(creator: () -> String) {
        logi(tag, creator)
    }

    /**
     * 内联 -> 消息懒加载
     */
    inline fun e(creator: () -> String) {
        loge(tag, creator)
    }

    /**
     * 内联 -> 消息懒加载
     */
    inline fun e(e: Throwable, creator: () -> String) {
        loge(tag, e, creator)
    }
}