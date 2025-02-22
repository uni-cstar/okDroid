package unics.okdroid.deprecated

import androidx.annotation.IntDef
import unics.okdroid.deprecated.util.log.UCSLog

/**
 * 标记：用于标注方法或者变量自动关联了生命周期
 */
@Retention(AnnotationRetention.SOURCE)
annotation class AutoLifecycle(val message: String = "使用了该注解的逻辑，支持自动管理生命周期")

@IntDef(value = [UCSLog.VERBOSE, UCSLog.DEBUG, UCSLog.INFO, UCSLog.WARN, UCSLog.ERROR])
@Retention(AnnotationRetention.SOURCE)
annotation class LogLevel