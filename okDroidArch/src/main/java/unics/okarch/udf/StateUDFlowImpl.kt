/**
 * @author: unics
 * @date: 2020/7/21
 * @desc: 轻量级单向数据流实现，符合mvi原则
 */
@file:JvmName("UDFFlow")
@file:JvmMultifileClass

package unics.okarch.udf

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 *
 * SSOTUDF: single source of truth ans unidirectional data flow
 * SSOT:single source of truth 单一数据源
 * UDF:unidirectional data flow 单向数据流
 * 一个UDF 对应的MVI架构所需要的定义
 * 参考链接：https://developer.android.com/topic/architecture?hl=zh-cn#single-source-of-truth
 */
interface StateUDFlow<Data : UDFData> : UDFlow<Data> {

    /**
     * 当前状态值
     */
    val value: Data

}


/**
 * 轻量级单向数据流实现；Intent的发送基于[Channel]实现，State的提供基于[shareIn]创造的ShareFlow实现
 * [Intent]输入的意图类型
 * [State]输出的状态类型
 * @param stateInitializer 初始状态构造器,懒加载；在
 */
internal class StateUDFlowImpl<State : UDFData>(
    scope: CoroutineScope,
    intentCapacity: Int = Channel.RENDEZVOUS,
    intentOnBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    intentOnUndeliveredElement: ((UDFIntent<State>) -> Unit)? = null,
    stateStarted: SharingStarted = SharingStarted.Eagerly,
    stateReplay: Int = 1,
    stateInitializer: () -> State
) : StateUDFlow<State> {

    //intent通道
    private val _intentChannel: Channel<UDFIntent<State>> =
        Channel(intentCapacity, intentOnBufferOverflow, intentOnUndeliveredElement)

    //state流
    @OptIn(FlowPreview::class)
    private val _uiStateFlow: SharedFlow<State> = _intentChannel.consumeAsFlow().flatMapConcat {
        log { "channel#cosumeFlow#flatMapConcat $it" }
        it.toDataFlow(_currentState)
    }.onEach {
        //缓存当前状态
        log { "channel#cosumeFlow#onEach cacheResult:$it" }
        _currentState = it
    }.shareIn(scope, stateStarted, stateReplay)

    //当前状态，支持延迟初始化
    private var _currentState: State by SynchronizedLazyStateProperty(stateInitializer)

    override val value: State get() = _currentState
    override suspend fun sendIntent(intent: UDFIntent<State>) {
        log { "sendIntent($intent)" }
        _intentChannel.send(intent)
    }

    /**
     * 如果没有接收者，此处可能会被挂起（受channel的配置影响）
     */
    override fun sendIntent(scope: CoroutineScope, intent: UDFIntent<State>) {
        log { "sendIntent($scope $intent)" }
        scope.launch {
            _intentChannel.send(intent)
        }
    }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) {
        log { "collect" }
        collector.emitAll(_uiStateFlow)
    }

}

/**
 * 采用同步锁初始化懒加载方式，参照[SynchronizedLazyImpl]实现
 */
private class SynchronizedLazyStateProperty<T : UDFData>(initializer: () -> T, lock: Any? = null) :
    ReadWriteProperty<Any, T> {

    private var initializer: (() -> T)? = initializer

    @Volatile
    private var _value: Any? = UNINITIALIZED_VALUE

    // final field is required to enable safe publication of constructed instance
    private val lock = lock ?: this

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val _v1 = _value
        if (_v1 !== UNINITIALIZED_VALUE) {
            @Suppress("UNCHECKED_CAST")
            return _v1 as T
        }

        return synchronized(lock) {
            val _v2 = _value
            if (_v2 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST") (_v2 as T)
            } else {
                log { "getStateValue --> from initializer" }
                val typedValue = initializer!!()
                _value = typedValue
                initializer = null
                typedValue
            }
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        _value = value
        initializer = null
    }

}

private object UNINITIALIZED_VALUE

@Suppress("SimpleDateFormat")
private val dateFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss.SSS")

internal inline fun log(msg: () -> String) {
    if (UDFLOG) {
        println("${dateFormat.format(Date())} UDFlow : ${msg.invoke()}")
    }
}
